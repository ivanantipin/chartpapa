package firelib.core.misc

import firelib.core.domain.InstrId
import firelib.finam.FinamDownloader
import firelib.core.store.MdStorageImpl
import firelib.core.InstrumentMapper
import firelib.core.misc.UtilsHandy.updateRussianStockSimple
import firelib.model.DivHelper
import java.time.Instant


class FinamTickerMapper(val finamDownloader: FinamDownloader) : InstrumentMapper{

    val symbols by lazy {
        finamDownloader.symbols()
    }

    val code2instr by lazy{
        symbols.groupBy { it.code.toLowerCase() }
    }

    override fun invoke(ticker: String): InstrId {
        val lst = code2instr[ticker.toLowerCase()]!!
        return lst.filter { it.market == FinamDownloader.SHARES_MARKET }.firstOrNull()!!
    }
}


object UtilsHandy {
    fun updateRussianDivStocks(): List<Pair<String, Instant>> {
        println("updating tickers that have a divs")
        val divs = DivHelper.getDivs()
        println("tickers to update ${divs.keys}")
        val storageImpl = MdStorageImpl()
        val finamDownloader = FinamDownloader()
        val symbols = finamDownloader.symbols().filter { divs.containsKey(it.code.toLowerCase()) && it.market == FinamDownloader.SHARES_MARKET }
        return symbols.map { Pair(it.code, storageImpl.updateMarketData(it)) }
    }

    fun updateRussianStockSimple(ticker: String) {
        val downloader = FinamDownloader()

        val symbols = downloader.symbols()

        val instr = symbols.find { it.code.equals(ticker, true) && it.market == FinamDownloader.SHARES_MARKET }

        if (instr != null) {
            println("updating instrument ${instr}")
            MdStorageImpl().updateMarketData(instr)
        } else {
            println("instrument not found ${ticker}")
        }
        downloader.close()

    }

}


fun main(args: Array<String>) {
    updateRussianStockSimple("sber")
//    updateRussianDivStocks()
}