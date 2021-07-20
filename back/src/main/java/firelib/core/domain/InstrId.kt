package firelib.core.domain

import firelib.core.SourceName
import java.math.BigDecimal


data class InstrId(val id: String = "N/A",
                   val name: String = "N/A",
                   val market: String = "NA",
                   val code: String = "N/A",
                   val source: String = "N/A",
                   val minPriceIncr: BigDecimal = BigDecimal.ONE.divide(10.toBigDecimal()),
                   val lot: Int = 1,
                   val board : String = "N/A"


){
    companion object{
        fun dummyInstrument(ticker : String) : InstrId {
            return InstrId(ticker, ticker, "", ticker, "dummy")
        }
        fun fromCodeAndExch(code : String, market : String) : InstrId{
            return InstrId(code = code, market = market, source = SourceName.FINAM.name)
        }

    }
}



fun InstrId.sourceEnum() : SourceName{
    return SourceName.valueOf(this.source)
}
