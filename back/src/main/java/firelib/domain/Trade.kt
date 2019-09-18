package firelib.common

import firelib.common.misc.dbl2Str
import firelib.domain.Side
import java.time.Instant


class TradeStat(val price : Double, val side : Side){

    var maxHoldingPrice: Double = price
    var minHoldingPrice: Double = price

    val factors =  HashMap<String,Double>()

    fun MAE(): Double {
        return if (this.side == Side.Sell) price - maxHoldingPrice else minHoldingPrice - price
    }

    fun MFE(): Double {
        return if (side == Side.Sell) price - minHoldingPrice else maxHoldingPrice - price
    }


    fun onPrice(pr: Double) {
        minHoldingPrice = Math.min(pr, minHoldingPrice)
        maxHoldingPrice = Math.max(pr, maxHoldingPrice)
    }

    fun addFactor(name: String, value: Double) {
        factors[name] = value
    }

}

data class Trade(val qty: Int, val price: Double, val order: Order, val dtGmt:Instant, val priceTime : Instant, val tradeStat: TradeStat = TradeStat(price, order.side)) {

    init {
        require(qty >= 0,{"amount can't be negative"})
        require(order != null,{"order must be present"})
        require(!price.isNaN() ,{"price must be valid"})
    }

    fun security ()= order.security

    fun side ()= order.side

    fun adjustPositionByThisTrade(position: Int): Int = position + order.side.sign * qty

    fun moneyFlow () : Double { return - qty * price * order.side.sign}

    fun split(amt: Int): Pair<Trade, Trade> {
        return Pair(copy(qty=amt), copy(qty=(qty - amt)))
    }

    override fun toString(): String {
        return "Trade(price=${dbl2Str(price, 2)} qty=$qty side=${side()} dtGmt=$dtGmt orderId=${order.id} sec=${security()})"
    }

}