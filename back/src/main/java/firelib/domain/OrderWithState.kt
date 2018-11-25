package firelib.domain

import firelib.common.Order
import firelib.common.OrderStatus
import firelib.common.Trade
import firelib.common.misc.SubChannel

class OrderWithState(val order: Order) {
    val statuses = mutableListOf(OrderStatus.New)
    val trades = ArrayList<Trade>()
    fun status() = statuses.last()
    fun remainingQty(): Int = order.qty - trades.map({ it -> it.qty }).sum()
}