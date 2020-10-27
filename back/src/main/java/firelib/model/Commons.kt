package firelib.model

import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants

val tickersToWrite = listOf(
    "sber",
    "lkoh",
    "gazp",
    "alrs",
    "moex",
    "gmkn",
    "mgnt",
    "rosn",
    "tatn",
    "tatnp",
    "sngs",
    "chmf",
    "aflt",
    "sberp",
    "nvtk",
    "vtbr",
    "hydr",
    "nlmk",
    "irao",
    "mtss",
    "magn",
    "sngsp",
    "plzl",
    "trnfp",
    "mtlr",
    "yndx",
    "rual",
    "fees",
    "sibn",
    "poly",
    "mrkp",
    "rtkm",
    "afks",
    "tcsg",
    "ogkb",
    "tgka",
    "rasp",
    "phor",
    "dsky",
    "lnta",
    "qiwi"
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

val tickers = populateTickersIfEmpty()



