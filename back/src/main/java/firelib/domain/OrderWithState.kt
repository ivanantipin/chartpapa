package firelib.domain

import firelib.common.Order
import firelib.common.OrderStatus
import firelib.common.Trade

class OrderWithState(val order: Order) {
    val statuses = mutableListOf(OrderStatus.New)
    val trades = mutableListOf<Trade>()
    fun status() = statuses.last()
    fun remainingQty(): Int = order.qtyLots - trades.sumBy { it.qty }
}