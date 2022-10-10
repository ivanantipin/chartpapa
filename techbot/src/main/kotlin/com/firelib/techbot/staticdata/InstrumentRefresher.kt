package com.firelib.techbot.staticdata

import com.firelib.techbot.mainLogger
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.store.MdStorageImpl
import java.util.concurrent.CompletableFuture

class InstrumentRefresher(val staticDataService: InstrumentsService){

    fun start(){
        val storage = MdStorageImpl()
        fetchSourceAsync(storage.sources[SourceName.MOEX], staticDataService)
        fetchSourceAsync(storage.sources[SourceName.FINAM], staticDataService)
        fetchSourceAsync(storage.sources[SourceName.POLIGON], staticDataService)
    }
    fun fetchSourceAsync(source: HistoricalSource, repo : InstrumentsService): CompletableFuture<String> {
        val ff = CompletableFuture<String>()
        val thr = Thread{
            var attepmt = 0
            while (true){
                try {
                    attepmt++
                    mainLogger.info("loading instruments for source ${source.getName()}")
                    loadSymbols(source, repo)
                    mainLogger.info("added source ${source}")
                    ff.completeAsync({""})
                    break
                }catch (e : Exception){
                    if(attepmt > 5){
                        ff.completeAsync({""})
                        break
                    }
                    mainLogger.error("failed to load insttruments from ${source.getName()} due to ${e.message} retrying in 5 min", e)
                    Thread.sleep(5*60_000)
                }
            }
        }
        thr.name = "InstrumentUpdater${source.getName()}"
        thr.isDaemon = true
        thr.start()
        return ff
    }

    private fun loadSymbols(source: HistoricalSource, repo: InstrumentsService) {
        val symbols = source.symbols()
        mainLogger.info("loaded instruments for ${source.getName()}, number is " + symbols.size)
        symbols.forEach {
            repo.putToCache(it)
        }
        repo.instrIdDao.addAll(symbols)
    }

}