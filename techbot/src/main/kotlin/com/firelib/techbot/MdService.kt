package com.firelib.techbot

import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import firelib.core.store.MdStorageImpl
import firelib.finam.FinamDownloader
import firelib.finam.MoexSource
import firelib.poligon.PoligonSource
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

object MdService {

    val log = LoggerFactory.getLogger(UsersNotifier::class.java)
    val pool = ForkJoinPool(1)
    val multiPool = ForkJoinPool(10)
    val storage = MdStorageImpl()

    val instruments = fetchInstruments()

    val liveSymbols = CopyOnWriteArrayList<InstrId>()
    val instrByCodeAndMarket = instruments.associateBy { Pair(it.code, it.market) }
    val id2inst = instruments.associateBy { it.id }
    val instrByStart = group(instruments)

    fun fetchInstruments(): List<InstrId> {
        val ret1 = PoligonSource().symbols()
        log.info("poligon instruments size " + ret1.size)
        return ret1 + loadFinam()
    }

    fun byId(id: String): InstrId {
        require(id2inst.containsKey(id), {"no instrument for id ${id}"})
        return id2inst[id]!!
    }

    fun group(instruments: List<InstrId>): Map<String, List<InstrId>> {
        val ret = instruments.groupBy { it.code.substring(0, 1) }
            .mapValues { it.value.sortedBy { it.code } }
            .toSortedMap()
        return ret
    }

    fun loadFinam(): List<InstrId> {
        val ret = FinamDownloader().symbols()
        log.info("finam instruments size " + ret.size)

        val futureSymbols = listOf(
            "BR",
            "RTS",
            "GOLD",
            "MIX",
            "Si",
        )

        val moexSymbols: Map<String, InstrId> = MoexSource().symbols().associateBy { it.code }

        return ret.map {
            if (it.code.length >= 2 &&
                (it.market == FinamDownloader.FX || it.market == FinamDownloader.SHARES_MARKET || (it.market == FinamDownloader.FinamMarket.FUTURES_MARKET.id && futureSymbols.contains(
                    it.code
                )))
            ) {
                if (moexSymbols.containsKey(it.code)) {
                    moexSymbols[it.code]!!
                } else {
                    it
                }
            } else {
                null
            }
        }.filterNotNull()
    }

    fun migrateSubscriptions() {
        val poligonSymbols = PoligonSource().symbols().associateBy { it.code }

        transaction {
            val symbols =
                Subscriptions.selectAll().filter { it[Subscriptions.market] in listOf("NASDAQ", "NYSE", "US") }
                    .map { it[Subscriptions.ticker] }.toSet()

            println("symbools to migrate")
            symbols.forEach {
                println("${it}")
            }

            symbols.forEach {
                val instrId = poligonSymbols[it]
                if (instrId != null) {
                    val cnt = Subscriptions.update({ Subscriptions.ticker eq instrId.code }) {
                        it[market] = instrId.market
                    }
                    println("updated ${instrId} count is ${cnt}")
                }
            }
        }
    }

    init {
        transaction {
            migrateSubscriptions()
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
            log.info("live instruments size is ${liveSymbols.size}")
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
        return pool.submit {
            measureAndLogTime("update md for ${instrId.code}") {
                storage.updateMarketData(instrId, Interval.Min10)
            }
        }
    }

    fun startMd() {
        Thread {
            timeSequence(Instant.now(), Interval.Min10, 10_000L).forEach {
                try {
                    updateAll()
                } catch (e: Exception) {
                    mainLogger.error("failed to update market data", e)
                }
            }
        }.start()
    }

    fun updateAll() {
        fun routeToRightPool(poo: ForkJoinPool, instrId: InstrId): ForkJoinTask<*> {
            return poo.submit({
                measureAndLogTime("update market data for instrument ${instrId.code}") {
                    storage.updateMarketData(instrId, Interval.Min10)
                }
            })
        }

        liveSymbols.map { instrId ->
            if (instrId.source == SourceName.FINAM.name) {
                routeToRightPool(pool, instrId)
            } else {
                routeToRightPool(multiPool, instrId)
            }
        }.forEach { it.get() }
    }
}

fun main() {
    initDatabase()
    transaction {
        MdService.migrateSubscriptions()
    }
}