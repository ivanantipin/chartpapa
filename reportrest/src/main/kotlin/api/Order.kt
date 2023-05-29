package firelib.stockviz.api

import firelib.core.domain.Side
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


data class Order(

    @get:Size(min = 1)
    val portfolio: String,

    @get:Size(min = 1)
    val orderId: String,

    val side: Side,

    val orderType: OrderType,

    val status: Status,

    val qty: BigDecimal,

    @get:NotNull
    val placeTime: Long,

    @get:NotNull
    val updateTime: Long,

    val symbol: String,

    val id: Int? = null,

    val discreteTags: Map<String, String> = emptyMap(),

    @field:Valid
    val continuousTags: Map<String, BigDecimal> = emptyMap(),

    @get:Size(min = 1)
    val tradeId: String,

    val price: BigDecimal,

    val executionPrice: BigDecimal
) {

    /**
     *
     * Values: limit,market,stop,stopLimit,marketOnClose,marketOnOpen,limitOnClose,limitOnOpen
     */
    enum class OrderType(val value: String) {

        limit("limit"),

        market("market"),

        stop("stop"),

        stopLimit("stop_limit"),

        marketOnClose("market_on_close"),

        marketOnOpen("market_on_open"),

        limitOnClose("limit_on_close"),

        limitOnOpen("limit_on_open");

    }

    /**
     *
     * Values: filled,canceled,placed,partialFilled,new,rejected
     */
    enum class Status(val value: String) {

        filled("filled"),

        canceled("canceled"),

        placed("placed"),

        partialFilled("partial_filled"),

        new("new"),

        rejected("rejected");

    }

}

