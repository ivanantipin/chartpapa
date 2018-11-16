package firelib.common

import java.time.Instant

import firelib.common.misc.utils

import scala.collection.mutable


class TradeStat(price : Double, side :Side){

    var maxHoldingPrice: Double = price
    var minHoldingPrice: Double = price

    val factors: collection.mutable.Map[String, String] = new mutable.HashMap[String,String]

    def MAE: Double = {
        if (side == Side.Sell) price - maxHoldingPrice else minHoldingPrice - price
    }

    def MFE: Double = {
        if (side == Side.Sell) price - minHoldingPrice else maxHoldingPrice - price
    }


    def onPrice(pr: Double) = {
        minHoldingPrice = math.min(pr, minHoldingPrice)
        maxHoldingPrice = math.max(pr, maxHoldingPrice)
    }

    def addFactor(name: String, value: String) = factors(name) = value

}

case class Trade(val qty: Int, val price: Double, val order: Order, val dtGmt:Instant) {

    assert(qty >= 0,"amount can't be negative")

    assert(order != null,"order must be present")

    assert(!price.isNaN ,"price must be valid")

    def security = order.security

    def side = order.side

    val tradeStat = new TradeStat(price, side)

    def adjustPositionByThisTrade(position: Int): Int = position + side.sign * qty

    def moneyFlow = - qty * price * side.sign

    def split(amt: Int): (Trade, Trade) = (copy(qty=amt), copy(qty=(qty - amt)))

    override def toString: String = {
        s"Trade(price=${utils.dbl2Str(price, 2)} qty=$qty side=$side dtGmt=$dtGmt orderId=${order.id} sec=$security)"
    }

}
