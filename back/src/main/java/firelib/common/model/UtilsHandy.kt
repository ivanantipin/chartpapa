package firelib.common.model

import com.funstat.domain.InstrId
import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.model.UtilsHandy.updateRussianStockSimple
import java.time.Instant


class FinamTickerMapper{
    val finamDownloader by lazy{
        FinamDownloader()
    }

    val symbols by lazy {
        finamDownloader.symbols()
    }

    val code2instr by lazy{
        symbols.groupBy { it.code.toLowerCase() }
    }

    fun map(ticker : String) : InstrId?{
        val lst = code2instr[ticker.toLowerCase()]!!
        return lst.filter { it.market == FinamDownloader.SHARES_MARKET }.firstOrNull()
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