package firelib.common

import firelib.core.domain.TradeStat
import firelib.core.misc.dbl2Str
import java.time.Instant


data class Trade(
    val qty: Int,
    val price: Double,
    val order: Order,
    val dtGmt: Instant,
    val priceTime: Instant,
    val tradeStat: TradeStat = TradeStat(
        price,
        order.side
    ),
    val positionAfter: Int = 0,
    val tradeNo : String = "na"
) {

    init {
        require(qty >= 0, { "amount can't be negative" })
        require(order != null, { "order must be present" })
        require(!price.isNaN(), { "price must be valid" })
    }

    fun security() = order.security

    fun side() = order.side

    fun adjustPositionByThisTrade(position: Int): Int = position + order.side.sign * qty

    fun moneyFlow(): Double {
        return -qty * price * order.side.sign
    }

    fun pnl(aPrice: Double): Double {
        return moneyFlow() - qty * aPrice * order.side.opposite().sign
    }

    fun split(amt: Int): Pair<Trade, Trade> {
        return Pair(copy(qty = amt), copy(qty = (qty - amt)))
    }

    override fun toString(): String {
        return "Trade(price=${dbl2Str(price,2)} qty=$qty side=${side()} dtGmt=$dtGmt orderId=${order.id} sec=${security()} posAfter=${positionAfter})"
    }

}