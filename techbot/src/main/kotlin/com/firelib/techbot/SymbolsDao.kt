package com.firelib.techbot

import firelib.core.domain.InstrId
import firelib.core.store.finamMapperWriter
import firelib.finam.FinamDownloader
import firelib.model.tickers


val fxSymbols = listOf(
    "EURUSD", "GBPUSD", "USDCHF", "AUDUSD", "NZDUSD"
)

val futureSymbols = listOf(
    "BR",
    "RTS",
    "GOLD",
    "MIX",
)

val batsSymbols = listOf(
    "SLB",
    "PBF",
    "HAL",
    "OXY",
    "BABA",
    "MSFT",
    "GOOG",
    "AMZN"
)


object SymbolsDao {

    fun fetchInstruments(): List<InstrId> {
        var ret = finamMapperWriter().read()
        if (ret.isEmpty()) {
            finamMapperWriter().write(FinamDownloader().symbols())
            ret = finamMapperWriter().read()
        }
        return ret
    }

    val instruments = fetchInstruments()
    val fxInstruments = instruments.filter { fxSymbols.contains(it.code) && it.market == FinamDownloader.FX }
    val stockInstruments =
        instruments.filter { tickers.contains(it.code) && it.market == FinamDownloader.SHARES_MARKET }
    val futInstruments =
        instruments.filter { futureSymbols.contains(it.code) && it.market == FinamDownloader.FUTURES_MARKET }
    val batsInstruments =
        instruments.filter { batsSymbols.contains(it.code) && it.market == FinamDownloader.BATS_MARKET }

    val all = fxInstruments + stockInstruments + futInstruments + batsInstruments


    fun available(): List<InstrId> {
        return all
    }

}