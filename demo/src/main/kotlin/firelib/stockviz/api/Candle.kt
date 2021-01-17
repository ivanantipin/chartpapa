package firelib.stockviz.api

import java.math.BigDecimal
import javax.validation.constraints.NotNull

data class Candle(

    @get:NotNull
    val datetime: Long,

    val open: BigDecimal,

    val high: BigDecimal,

    val low: BigDecimal,

    val close: BigDecimal,

    @get:NotNull
    val volume: Int
)

