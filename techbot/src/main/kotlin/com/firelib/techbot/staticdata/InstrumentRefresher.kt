package com.firelib.techbot.staticdata

import com.firelib.techbot.mainLogger
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.store.MdStorageImpl
import firelib.finam.FinamDownloader
import kotlinx.coroutines.*

class InstrumentRefresher(val staticDataService: InstrumentsService) {

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        val storage = MdStorageImpl()
        scope.launch {  fetchSourceAsync(storage.sources[SourceName.MOEX], staticDataService)}
//        scope.launch {  fetchSourceAsync(storage.sources[SourceName.POLIGON], staticDataService)}
    }

    suspend fun fetchSourceAsync(source: HistoricalSource, repo: InstrumentsService, filter : (InstrId)->Boolean = {true}){
        var attepmt = 0
        while (true) {
            try {
                attepmt++
                mainLogger.info("loading instruments for source ${source.getName()}")
                loadSymbols(source, repo)
                mainLogger.info("added source ${source}")
                break
            } catch (e: Exception) {
                if (attepmt > 5) {
                    break
                }
                mainLogger.error(
                    "failed to load instruments from ${source.getName()} due to ${e.message} retrying in 5 min",
                    e
                )
                delay(5 * 60_000)
            }
        }
    }

    private suspend fun loadSymbols(source: HistoricalSource, repo: InstrumentsService) {
        val symbols = source.symbols()
        mainLogger.info("loaded instruments for ${source.getName()}, number is " + symbols.size)
        symbols.forEach {
            repo.putToCache(it)
        }
        repo.instrIdDao.replaceSourceInstruments(symbols)
    }

}
