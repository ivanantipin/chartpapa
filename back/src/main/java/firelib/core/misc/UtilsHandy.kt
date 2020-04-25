package firelib.core.misc

import firelib.core.domain.Interval
import firelib.core.store.MdStorageImpl
import firelib.finam.FinamDownloader
import firelib.model.DivHelper
import org.slf4j.LoggerFactory
import java.time.Instant

object UtilsHandy {

    val log = LoggerFactory.getLogger(javaClass)

    fun updateRussianDivStocks(market: String = FinamDownloader.SHARES_MARKET): List<Pair<String, Instant>> {
        log.info("updating tickers that have a divs")
        val divs = DivHelper.getDivs()
        log.info("tickers to update ${divs.keys}")
        val storageImpl = MdStorageImpl()
        val finamDownloader = FinamDownloader()
        val symbols =
            finamDownloader.symbols().filter { divs.containsKey(it.code.toLowerCase()) && it.market == market }
        return symbols.map { Pair(it.code, storageImpl.updateMarketData(it, Interval.Min10)) }
    }


    fun updateTicker(ticker: String, market: String = FinamDownloader.SHARES_MARKET) {
        val downloader = FinamDownloader()

        val symbols = downloader.symbols()

        val instr = symbols.find { it.code.equals(ticker, true) && it.market == market }

        if (instr != null) {
            log.info("updating instrument ${instr}")
            MdStorageImpl().updateMarketData(instr, Interval.Min10)
        } else {
            log.info("instrument not found ${ticker}")
        }
        downloader.close()

    }

}



fun main(args: Array<String>) {
//    MdStorageImpl().updateMarketData(InstrId(code = "ALLFUTSi", source = SourceName.MT5.name), interval = Interval.Min15);
    UtilsHandy.updateTicker("SBER")
    //UtilsHandy.updateTicker("irao")
}