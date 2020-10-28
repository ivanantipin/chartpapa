package com.firelib.techbot

import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import firelib.core.store.MdStorageImpl
import firelib.core.store.finamMapperWriter
import firelib.finam.FinamDownloader
import firelib.model.tickers
import java.time.Instant


val fxSymbols = listOf(
    "EURUSD", "GBPUSD", "USDCHF", "AUDUSD", "NZDUSD"
)


object SymbolsDao {

    fun fetchInstruments(): List<InstrId> {
        var ret = finamMapperWriter().read()
        if(ret.isEmpty()){
            finamMapperWriter().write(FinamDownloader().symbols())
            ret = finamMapperWriter().read()
        }
        return ret
    }

    val instruments = fetchInstruments()
    val fxInstruments = instruments.filter { fxSymbols.contains(it.code) && it.market == FinamDownloader.FX }
    val stockInstruments = instruments.filter { tickers.contains(it.code.toLowerCase()) && it.market == FinamDownloader.SHARES_MARKET }

    val all = fxInstruments + stockInstruments

    init {

        Thread({
            timeSequence(Instant.now(), Interval.Min10, 10_000L).forEach {
                try {
                    val storage = MdStorageImpl()
                    available().forEach {
                        storage.updateMarketData(it, Interval.Min10)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }).start()
    }


    fun available(): List<InstrId> {
        return all
    }

}