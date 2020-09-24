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

val tickers = tickersReader.read().map { it.ticker }

fun main() {
    //writer.write(tickers.map{TickerConfig(it)})
    println(tickersReader.read())
}








