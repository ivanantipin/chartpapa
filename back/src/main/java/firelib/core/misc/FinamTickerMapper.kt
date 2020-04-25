package firelib.core.misc

import firelib.core.InstrumentMapper
import firelib.core.domain.InstrId
import firelib.finam.FinamDownloader

class FinamTickerMapper(val finamDownloader: FinamDownloader) :
    InstrumentMapper {

    val symbols by lazy {
        finamDownloader.symbols()
    }

    val code2instr by lazy {
        symbols.groupBy { it.code.toLowerCase() }
    }

    override fun invoke(ticker: String): InstrId? {

        val lticker = ticker.toLowerCase()
        require(code2instr.containsKey(lticker), {"no ticker found ${lticker}"})

        val lst = code2instr[lticker]!!
        return lst.filter { it.market == FinamDownloader.SHARES_MARKET }.firstOrNull()
    }
}