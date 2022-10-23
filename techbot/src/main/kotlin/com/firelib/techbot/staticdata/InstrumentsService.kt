package com.firelib.techbot.staticdata

import com.firelib.techbot.mainLogger
import com.firelib.techbot.usernotifier.UsersNotifier
import firelib.core.domain.InstrId
import firelib.finam.FinamDownloader
import firelib.finam.MoexSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InstrumentsService(val instrIdDao: InstrIdDao) {

    val log: Logger = LoggerFactory.getLogger(UsersNotifier::class.java)

    val instrByCodeAndMarket = ConcurrentHashMap<Pair<String, String>, InstrId>()
    val id2inst = ConcurrentHashMap<String, InstrId>()
    val instrumentByFirstCharacter = ConcurrentHashMap<String, MutableMap<String, InstrId>>()

    init {
        initialLoad()
    }

    private fun initialLoad() {
        instrIdDao.loadAll().forEach {
            putToCache(it)
        }
    }

    fun putToCache(instrId: InstrId) {
        if (instrId.code.isBlank()) {
            mainLogger.info("not adding instrument ${instrId} as code is blank")
            return
        }
        instrByCodeAndMarket.put(instrId.code to instrId.market, instrId)
        id2inst[instrId.id] = instrId
        val mp = instrumentByFirstCharacter.computeIfAbsent(instrId.code.first().toString(), {
            Collections.synchronizedMap(TreeMap<String, InstrId>())
        })
        mp[instrId.code] = instrId
    }

    fun byId(id: String): InstrId {
        require(id2inst.containsKey(id), { "no instrument for id ${id}" })
        return id2inst[id]!!
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
}