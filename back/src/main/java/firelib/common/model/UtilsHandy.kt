package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.model.UtilsHandy.updateRussianDivStocks


object UtilsHandy{
    fun updateRussianDivStocks(){
        println("updating tickers that have a divs")
        val divs = DivHelper.getDivs()
        println("tickers to update ${divs.keys}")
        val storageImpl = MdStorageImpl()
        val finamDownloader = FinamDownloader()
        val symbols = finamDownloader.symbols().filter { divs.containsKey(it.code.toLowerCase()) && it.market == "1" }
        symbols.forEach({ storageImpl.updateMarketData(it) })
    }

    fun updateRussianStockSimple(ticker : String){
        val downloader = FinamDownloader()

        val symbols = downloader.symbols()

        val instr = symbols.find { it.code.equals(ticker,true) && it.market == FinamDownloader.SHARES_MARKET }

        if(instr != null){
            println("updating instrument ${instr}")
            MdStorageImpl().updateMarketData(instr)
        }else{
            println("instrument not found ${ticker}")
        }
        downloader.close()

    }

}


fun main(args: Array<String>) {
    //updateRussianStockSimple("sber")
    updateRussianDivStocks()
}