package firelib.model

import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants

val tickersToWrite = listOf(
    "SBER",
    "LKOH",
    "GAZP",
    "ALRS",
    "MOEX",
    "GMKN",
    "MGNT",
    "ROSN",
    "TATN",
    "TATNP",
    "SNGS",
    "CHMF",
    "AFLT",
    "SBERP",
    "NVTK",
    "VTBR",
    "HYDR",
    "NLMK",
    "IRAO",
    "MTSS",
    "MAGN",
    "SNGSP",
    "PLZL",
    "TRNFP",
    "MTLR",
    "YNDX",
    "RUAL",
    "FEES",
    "SIBN",
    "POLY",
    "MRKP",
    "RTKM",
    "AFKS",
    "TCSG",
    "OGKB",
    "TGKA",
    "RASP",
    "PHOR",
    "DSKY",
    "LNTA",
    "QIWI"
)

data class TickerConfig(val ticker : String)

val tickersReader = GeGeWriter<TickerConfig>(GlobalConstants.metaDb, TickerConfig::class, listOf("ticker"), "ticker_config")

fun populateTickersIfEmpty() : List<String>{
    var rr = tickersReader.read().map { it.ticker }
    if(rr.isEmpty()){
        println("populating tickers")
        tickersReader.write(tickersToWrite.map{TickerConfig(it)})
        rr = tickersReader.read().map { it.ticker }
    }
    return rr
}

val tickers = populateTickersIfEmpty().sorted()
