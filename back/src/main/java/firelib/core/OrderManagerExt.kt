package firelib.core

import firelib.common.Order
import firelib.core.domain.OrderType
import firelib.core.domain.Side
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.concurrent.TimeUnit

fun OrderManager.makePositionEqualsTo(pos: Int, price : Double? = null) : Order?{
    if(this.hasPendingState()){
        return null
    }
    var diff = getOrderForDiff(this.position(), pos)
    if(diff != null){
        if(price != null){
            val pr =  if (diff.side == Side.Buy) price * 0.97 else price * 1.05
            val roundPrice = pr.roundPrice(instrument().minPriceIncr)
            diff = diff.copy(price = roundPrice)

            println("round price is ${roundPrice}")

        }
        this.submitOrders(diff)
    }
    return diff
}





fun OrderManager.buyAtLimit(price: Double, vol: Int = 1) {
    this.submitOrders(Order(OrderType.Limit, price, vol, Side.Buy, security(), nextOrderId(),currentTime(), instrument(), modelName()))
}

fun OrderManager.sellAtLimit(price: Double, vol: Int = 1) {
    submitOrders(Order(OrderType.Limit, price, vol, Side.Sell, security(), nextOrderId(), currentTime(), instrument(), modelName()))
}

fun OrderManager.sellAtMarket(vol: Int = 1) {
    submitOrders(Order(OrderType.Market, 0.0, vol, Side.Sell, security(), nextOrderId(), currentTime(), instrument(), modelName()))
}

fun OrderManager.buyAtStop(price: Double, vol: Int = 1) {
    submitOrders(Order(OrderType.Stop, price, vol, Side.Buy, security(), nextOrderId(), currentTime(), instrument(), modelName()))
}

fun OrderManager.sellAtStop(price: Double, vol: Int = 1) {
    submitOrders(Order(OrderType.Stop, price, vol, Side.Sell, security(), nextOrderId(), currentTime(), instrument(), modelName()))
}

fun OrderManager.getOrderForDiff(currentPosition: Int, targetPos: Int): Order? {
    val vol = targetPos - currentPosition
    if (vol != 0) {
        return Order(OrderType.Market, 0.0, Math.abs(vol), if (vol > 0) Side.Buy else Side.Sell, security(), nextOrderId(), currentTime(), instrument(), modelName())
    }
    return null
}

fun OrderManager.flattenAll(reason: String? = null) {
    cancelAllOrders()
    makePositionEqualsTo(0)
}

fun OrderManager.cancelAllOrders() {
    liveOrders().filter({ o->o.orderType != OrderType.Market}).forEach {
        cancelOrders(it)
    }
}



fun Double.roundPrice(incr : BigDecimal) : Double{
    val price = this.toBigDecimal().divide(incr, RoundingMode.HALF_UP)
    return incr.multiply(price.toInt().toBigDecimal()).toDouble()
}

fun OrderManager.positionDuration(now : Instant, unit : TimeUnit = TimeUnit.HOURS) : Long {
    return (now.toEpochMilli() - positionTime().toEpochMilli())/unit.toMillis(1)
}
