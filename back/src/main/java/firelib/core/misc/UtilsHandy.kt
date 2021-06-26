package firelib.core.misc

import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.store.MdStorageImpl
import firelib.core.store.finamMapperWriter
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
            finamDownloader.symbols().filter { tickers.contains(it.code.toUpperCase()) && it.market == market }
        return symbols.map { Pair(it.code, storageImpl.updateMarketData(it, interval)) }
    }


    fun updateTicker(ticker: String, market: String = FinamDownloader.SHARES_MARKET, interval : Interval = Interval.Min1) {
        val downloader = FinamDownloader()
        val symbols = downloader.symbols()
        val instr = symbols.find { it.code.equals(ticker, true) }
        if (instr != null) {
            log.info("updating instrument ${instr}")
            MdStorageImpl().updateMarketData(instr, interval)
        } else {
            log.info("instrument not found ${ticker}")
        }
        downloader.close()
    }

}


fun main(args: Array<String>) {
    //finamMapperWriter().write(FinamDownloader().symbols())
    //UtilsHandy.updateTicker("UVXY", FinamDownloader.ETF_MARKET, interval = Interval.Min10)

    MdStorageImpl().updateMarketData(InstrId(source = SourceName.FINAM.name,
        code = "TSLA",
        market = FinamDownloader.BATS_MARKET), interval = Interval.Min10)


}