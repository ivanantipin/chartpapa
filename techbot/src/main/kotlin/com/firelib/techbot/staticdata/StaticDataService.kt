package com.firelib.techbot.staticdata

import com.firelib.techbot.UsersNotifier
import com.firelib.techbot.mainLogger
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.store.MdStorageImpl
import firelib.finam.FinamDownloader
import firelib.finam.MoexSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool

class StaticDataService(val instrIdDao: InstrIdDao) {

    val log: Logger = LoggerFactory.getLogger(UsersNotifier::class.java)

    val instrByCodeAndMarket = ConcurrentHashMap<Pair<String, String>, InstrId>()
    val id2inst = ConcurrentHashMap<String, InstrId>()
    val instrumentByFirstCharacter = ConcurrentHashMap<String, MutableMap<String, InstrId>>()

    fun start(){
        instrIdDao.loadAll().forEach {
            putToCache(it)
        }
        val storage = MdStorageImpl()
        fetchSourceAsync(storage.sources[SourceName.MOEX], this)
        fetchSourceAsync(storage.sources[SourceName.FINAM], this)
        fetchSourceAsync(storage.sources[SourceName.POLIGON], this)

    }


    fun putToCache(instrId: InstrId){
        instrByCodeAndMarket.put(instrId.code to instrId.market , instrId)
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

    fun fetchSourceAsync(source: HistoricalSource, repo : StaticDataService): CompletableFuture<String> {
        val ff = CompletableFuture<String>()
        ForkJoinPool.commonPool().submit {
            var attepmt = 0
            while (true){
                try {
                    attepmt++
                    val symbols = source.symbols()
                    mainLogger.info("${source.getName()} instruments size " + symbols.size)
                    symbols.forEach {
                        repo.putToCache(it)
                    }
                    repo.instrIdDao.add(symbols)
                    ff.completeAsync({""})
                    break
                }catch (e : Exception){
                    if(attepmt > 5){
                        ff.completeAsync({""})
                        break
                    }
                    mainLogger.error("failed to load insttruments from ${source.getName()} due to ${e.message} retrying in 5 min")
                    Thread.sleep(5*60_000)
                }
            }
        }
        return ff
    }
}