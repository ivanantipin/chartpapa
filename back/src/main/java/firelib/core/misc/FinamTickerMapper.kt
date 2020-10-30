package firelib.core.misc

import firelib.core.InstrumentMapper
import firelib.core.domain.InstrId
import firelib.finam.FinamDownloader

class FinamTickerMapper(val finamDownloader: FinamDownloader, val market : String = FinamDownloader.SHARES_MARKET) :
    InstrumentMapper {

    val symbols by lazy {
        finamDownloader.symbols()
    }

    val code2instr by lazy {
        symbols.groupBy { it.code.toLowerCase() }
    }

    override fun invoke(ticker: String): InstrId? {
        require(code2instr.containsKey(ticker), {"no ticker found ${ticker}"})
        val lst = code2instr[ticker]!!
        return lst.filter { it.market == market }.firstOrNull()
    }
}