package firelib.common.ordermanager

import firelib.common.Order
import firelib.common.OrderType
import firelib.common.Side

fun OrderManager.managePosTo(pos: Int): Unit {
    if(this.hasPendingState()){
        return
    }
    val diff = getOrderForDiff(this.position(), pos)
    if(diff != null){
        this.submitOrders(listOf(diff))
    }
}

fun OrderManager.buyAtLimit(price: Double, vol: Int = 1) {
    this.submitOrders(listOf(Order(OrderType.Limit, price, vol, Side.Buy, security(), nextOrderId(),currentTime())))
}

fun OrderManager.sellAtLimit(price: Double, vol: Int = 1) {
    submitOrders(listOf(Order(OrderType.Limit, price, vol, Side.Sell, security(), nextOrderId(), currentTime())))
}

fun OrderManager.buyAtStop(price: Double, vol: Int = 1) {
    submitOrders(listOf(Order(OrderType.Stop, price, vol, Side.Buy, security(), nextOrderId(), currentTime())))
}

fun OrderManager.sellAtStop(price: Double, vol: Int = 1) {
    submitOrders(listOf(Order(OrderType.Stop, price, vol, Side.Sell, security(), nextOrderId(), currentTime())))
}

fun OrderManager.getOrderForDiff(currentPosition: Int, targetPos: Int): Order? {
    val vol = targetPos - currentPosition
    if (vol != 0) {
        return Order(OrderType.Market, 0.0, Math.abs(vol), if (vol > 0) Side.Buy else Side.Sell, security(), nextOrderId(), currentTime())
    }
    return null
}

fun OrderManager.flattenAll(reason: String? = null) {
    cancelAllOrders()
    managePosTo(0)
}

fun OrderManager.cancelAllOrders() {cancelOrders(liveOrders().filter({o->o.orderType != OrderType.Market}))}
