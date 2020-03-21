package firelib.core.misc

import firelib.core.domain.InstrId
import firelib.finam.FinamDownloader
import firelib.core.store.MdStorageImpl
import firelib.core.InstrumentMapper
import firelib.core.misc.UtilsHandy.updateTicker
import firelib.core.store.finamMapperWriter
import firelib.model.DivHelper
import org.slf4j.LoggerFactory
import java.time.Instant


class FinamTickerMapper(val finamDownloader: FinamDownloader) : InstrumentMapper{

    val symbols by lazy {
        finamDownloader.symbols()
    }

    val code2instr by lazy{
        symbols.groupBy { it.code.toLowerCase() }
    }

    override fun invoke(ticker: String): InstrId? {
        val lst = code2instr[ticker.toLowerCase()]!!
        return lst.filter { it.market == FinamDownloader.SHARES_MARKET }.firstOrNull()
    }
}


object UtilsHandy {

    val log = LoggerFactory.getLogger(javaClass)

    fun updateRussianDivStocks(market : String = FinamDownloader.SHARES_MARKET): List<Pair<String, Instant>> {
        log.info("updating tickers that have a divs")
        val divs = DivHelper.getDivs()
        log.info("tickers to update ${divs.keys}")
        val storageImpl = MdStorageImpl()
        val finamDownloader = FinamDownloader()
        val symbols = finamDownloader.symbols().filter { divs.containsKey(it.code.toLowerCase()) && it.market == market }
        return symbols.map { Pair(it.code, storageImpl.updateMarketData(it)) }
    }

    fun updateTicker(ticker: String, market : String = FinamDownloader.SHARES_MARKET) {
        val downloader = FinamDownloader()

        val symbols = downloader.symbols()

        val instr = symbols.find { it.code.equals(ticker, true) && it.market == market }

        if (instr != null) {
            log.info("updating instrument ${instr}")
            MdStorageImpl().updateMarketData(instr)
        } else {
            log.info("instrument not found ${ticker}")
        }
        downloader.close()

    }

}


fun main(args: Array<String>) {

    updateTicker("SPFB.Si", "14")
//    updateRussianDivStocks()
}