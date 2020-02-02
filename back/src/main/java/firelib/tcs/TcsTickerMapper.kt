package firelib.tcs

import firelib.core.domain.InstrId

class TcsTickerMapper {

    val context by lazy {
        getContext()
    }

    val source by lazy {
        TcsHistoricalSource(context)
    }

    val code2instr by lazy {
        source.symbols().groupBy { it.code.toLowerCase() }
    }

    fun map(ticker: String): InstrId? {
        val lst = code2instr[ticker.toLowerCase()]!!
        return lst.firstOrNull()
    }
}