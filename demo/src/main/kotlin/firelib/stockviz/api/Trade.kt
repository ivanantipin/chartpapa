package firelib.stockviz.api

import firelib.core.domain.Side
import java.math.BigDecimal
import javax.validation.constraints.NotNull

data class Trade(

    val tradeId: String,

    val portfolio: String,

    val side: Side,

    val qty: BigDecimal,

    @get:NotNull
    val openTime: Long,

    @get:NotNull
    val closeTime: Long,

    val openPrice: BigDecimal,

    val closePrice: BigDecimal,

    val pnl: BigDecimal,

    val symbol: String,

    val discreteTags: Map<String, String> = emptyMap(),

    val continuousTags: Map<String, BigDecimal> = emptyMap()
)

