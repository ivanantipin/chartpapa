package com.firelib.techbot

import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import firelib.core.store.MdStorageImpl
import firelib.core.store.finamMapperWriter
import firelib.finam.FinamDownloader
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.concurrent.*

object MdService {

    val pool = ForkJoinPool(1)
    val storage = MdStorageImpl()

    val liveSymbols = CopyOnWriteArrayList<InstrId>()


    fun fetchInstruments(): List<InstrId> {
        var ret = finamMapperWriter().read()
        if (ret.isEmpty()) {
            finamMapperWriter().write(FinamDownloader().symbols())
            ret = finamMapperWriter().read()
        }
        return ret
    }

    fun byId(id : String) : InstrId{
        return id2inst[id]!!
    }

    val instrByCodeAndMarket = fetchInstruments().associateBy { Pair(it.code, it.market) }
    val id2inst = fetchInstruments().associateBy { it.id }

    val instrByStart = group(fetchInstruments())

    fun group(instruments: List<InstrId>): Map<String, List<InstrId>> {

        val futureSymbols = listOf(
            "BR",
            "RTS",
            "GOLD",
            "MIX",
            "Si",
        )

        val toSet = FinamDownloader.FinamMarket.values()
            .map { it.id }.toSet()

        return instruments.filter {
            toSet.contains(it.market) && it.code.length >= 2
                    && (it.market != FinamDownloader.FinamMarket.FUTURES_MARKET.id || futureSymbols.contains(it.code))

        }.groupBy { it.code.substring(0, 1) }
            .mapValues { it.value.sortedBy { it.code } }
            .toSortedMap()
    }

    init {

        transaction {
            val live =
                Subscriptions.selectAll().map { Pair(it[Subscriptions.ticker], it[Subscriptions.market]) }.distinct()
            liveSymbols.addAll(live.flatMap { key ->
                val orDefault = instrByCodeAndMarket.getOrDefault(key, null)
                if (orDefault == null) {
                    mainLogger.error("failed to map ${key}")
                    emptyList()
                } else {
                    listOf(orDefault)
                }
            })
        }

    }

    fun update(instr: InstrId): CompletableFuture<Unit>? {
        if (!liveSymbols.any { it.code == instr.code && it.market == instr.market }) {
            liveSymbols.add(instr)
            return CompletableFuture.supplyAsync({ updateStock(instr).get() })
        }
        return null
    }

    fun updateStock(instrId: InstrId): ForkJoinTask<*> {
        return pool.submit({
            measureAndLogTime("update md for ${instrId.code}") {
                storage.updateMarketData(instrId, Interval.Min10)
            }
        })
    }

    fun startMd() {
        Thread({

            //Subscriptions.selectAll().distinctBy { Subscriptions.ticker }.
            timeSequence(Instant.now(), Interval.Min10, 10_000L).forEach {
                try {
                    updateAll()
                } catch (e: Exception) {
                    mainLogger.error("failed to update market data", e)
                }
            }
        }).start()
    }

    fun updateAll() {
        pool.submit({
            liveSymbols.forEach {
                measureAndLogTime("update market data for instrument ${it.code}") {
                    storage.updateMarketData(it, Interval.Min10)
                }
            }
        }).get()
    }

}

fun main() {
    initDatabase()

    transaction {
        MdService.group(MdService.fetchInstruments()).forEach { s, list ->
            println("size of ${s} is ${list.size}")
        }

    }

}
