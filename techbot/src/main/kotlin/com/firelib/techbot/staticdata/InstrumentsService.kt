package com.firelib.techbot.staticdata

import com.firelib.techbot.mainLogger
import com.firelib.techbot.usernotifier.UsersNotifier
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.finam.FinamDownloader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class InstrumentsService(val instrIdDao: InstrIdDao) {

    val log: Logger = LoggerFactory.getLogger(UsersNotifier::class.java)

    val instrByCodeAndMarket = ConcurrentHashMap<Pair<String, String>, InstrId>()
    val id2inst = ConcurrentHashMap<String, InstrId>()


    init {
        initialLoad()
    }

    fun filter(it: InstrId):  Boolean{
        if(it.source == SourceName.FINAM.name){
            val futureSymbols = listOf(
                "BR",
                "RTS",
                "GOLD",
                "MIX",
                "Si",
            )

            return it.code.length >= 2 &&
                    (it.market == FinamDownloader.FX || it.market == FinamDownloader.SHARES_MARKET ||
                            (it.market == FinamDownloader.FinamMarket.FUTURES_MARKET.id
                                    && futureSymbols.contains( it.code)))

        }
        return true

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
        if(!filter(instrId)){
            mainLogger.info("not adding instrument ${instrId} as it is filtered")
            return
        }
        instrByCodeAndMarket.put(instrId.code to instrId.market, instrId)
        id2inst[instrId.id] = instrId
    }

    fun byId(id: String): InstrId {
        require(id2inst.containsKey(id), { "no instrument for id ${id}" })
        return id2inst[id]!!
    }

}