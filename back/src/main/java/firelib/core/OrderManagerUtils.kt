package firelib.core

import firelib.common.Order
import firelib.core.domain.OrderType
import firelib.core.domain.Side
import java.math.BigDecimal
import java.math.RoundingMode

fun OrderManager.makePositionEqualsTo(pos: Int, price : Double? = null) {
    if(this.hasPendingState()){
        return
    }
    var diff = getOrderForDiff(this.position(), pos)
    if(diff != null){
        if(price != null){
            val pr =  if (diff.side == Side.Buy) price * 0.97 else price * 1.05
            val roundPrice = pr.roundPrice(instrument().minPriceIncr)
            diff = diff.copy(price = roundPrice)

            println("round price is ${roundPrice}")

        }
        this.submitOrders(listOf(diff))
    }
}



fun OrderManager.buyAtLimit(price: Double, vol: Int = 1) {
    this.submitOrders(listOf(Order(OrderType.Limit, price, vol, Side.Buy, security(), nextOrderId(),currentTime(), instrument())))
}

fun OrderManager.sellAtLimit(price: Double, vol: Int = 1) {
    submitOrders(listOf(Order(OrderType.Limit, price, vol, Side.Sell, security(), nextOrderId(), currentTime(), instrument())))
}

fun OrderManager.sellAtMarket(vol: Int = 1) {
    submitOrders(listOf(Order(OrderType.Market, 0.0, vol, Side.Sell, security(), nextOrderId(), currentTime(), instrument())))
}

fun OrderManager.buyAtStop(price: Double, vol: Int = 1) {
    submitOrders(listOf(Order(OrderType.Stop, price, vol, Side.Buy, security(), nextOrderId(), currentTime(), instrument())))
}

fun OrderManager.sellAtStop(price: Double, vol: Int = 1) {
    submitOrders(listOf(Order(OrderType.Stop, price, vol, Side.Sell, security(), nextOrderId(), currentTime(), instrument())))
}

fun OrderManager.getOrderForDiff(currentPosition: Int, targetPos: Int): Order? {
    val vol = targetPos - currentPosition
    if (vol != 0) {
        return Order(OrderType.Market, 0.0, Math.abs(vol), if (vol > 0) Side.Buy else Side.Sell, security(), nextOrderId(), currentTime(), instrument())
    }
    return null
}

fun OrderManager.flattenAll(reason: String? = null) {
    cancelAllOrders()
    makePositionEqualsTo(0)
}

fun OrderManager.cancelAllOrders() {cancelOrders(liveOrders().filter({ o->o.orderType != OrderType.Market}))}



fun Double.roundPrice(incr : BigDecimal) : Double{
    val price = this.toBigDecimal().divide(incr, RoundingMode.HALF_UP)
    return incr.multiply(price.toInt().toBigDecimal()).toDouble()
}