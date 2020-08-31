package firelib.core.misc

import firelib.core.domain.Interval
import firelib.core.store.MdStorageImpl
import firelib.finam.FinamDownloader
import firelib.model.tickers
import org.slf4j.LoggerFactory
import java.time.Instant

object UtilsHandy {

    val log = LoggerFactory.getLogger(javaClass)

    fun updateRussianDivStocks(
        market: String = FinamDownloader.SHARES_MARKET,
        interval: Interval
    ): List<Pair<String, Instant>> {
        log.info("updating tickers that have a divs")
        log.info("tickers to update ${tickers}")
        val storageImpl = MdStorageImpl()
        val finamDownloader = FinamDownloader()
        val symbols =
            finamDownloader.symbols().filter { tickers.contains(it.code.toLowerCase()) && it.market == market }
        return symbols.map { Pair(it.code, storageImpl.updateMarketData(it, interval)) }
    }


    fun updateTicker(ticker: String, market: String = FinamDownloader.SHARES_MARKET) {
        val downloader = FinamDownloader()
        val symbols = downloader.symbols()
        val instr = symbols.find { it.code.equals(ticker, true) && it.market == market }
        if (instr != null) {
            log.info("updating instrument ${instr}")
            MdStorageImpl().updateMarketData(instr, Interval.Min1)
        } else {
            log.info("instrument not found ${ticker}")
        }
        downloader.close()
    }

}


fun main(args: Array<String>) {
//    MdStorageImpl().updateMarketData(InstrId(code = "ALLFUTSi", source = SourceName.MT5.name), interval = Interval.Min15);
    //UtilsHandy.updateTicker("sngsp")

    UtilsHandy.updateRussianDivStocks(interval = Interval.Min10)

    return


//    val impl = MdStorageImpl()
//    tickers.forEach {
//        impl.updateMarketData(InstrId(code = it.toUpperCase(), board = "TQBR", source = SourceName.MOEX.name), Interval.Min5)
//    }

    val downloader = FinamDownloader()
    val symbols = downloader.symbols()

    symbols.filter {
        it.code.contains("ogkb", ignoreCase = true) || it.name.contains("гдр", ignoreCase = true)
    }.forEach { println(it) }


    UtilsHandy.updateTicker("lnta")
//    UtilsHandy.updateRussianDivStocks(interval = Interval.Min1)


    //UtilsHandy.updateTicker("irao")
}