package firelib.common.ordermanager

import firelib.common.Order
import firelib.domain.OrderType
import firelib.domain.Side

fun OrderManager.makePositionEqualsTo(pos: Int, price : Double? = null) {
    if(this.hasPendingState()){
        return
    }
    var diff = getOrderForDiff(this.position(), pos)
    if(diff != null){
        if(price != null){
            diff = diff.copy(price = if(diff.side == Side.Buy)  price*1.3 else price*0.97)
        }
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
    makePositionEqualsTo(0)
}

fun OrderManager.cancelAllOrders() {cancelOrders(liveOrders().filter({o->o.orderType != OrderType.Market}))}
