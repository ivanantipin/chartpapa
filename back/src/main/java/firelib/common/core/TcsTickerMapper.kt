package firelib.common.core

import com.funstat.domain.InstrId
import com.funstat.tcs.TcsSource
import com.funstat.tcs.getContext

class TcsTickerMapper {
    val source by lazy {
        TcsSource(getContext())
    }

    val code2instr by lazy {
        source.symbols().groupBy { it.code.toLowerCase() }
    }

    fun map(ticker: String): InstrId? {
        val lst = code2instr[ticker.toLowerCase()]!!
        return lst.firstOrNull()
    }
}