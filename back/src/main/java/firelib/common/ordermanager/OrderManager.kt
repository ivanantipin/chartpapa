package firelib.common.ordermanager

import firelib.common.Order
import firelib.common.Trade
import firelib.common.misc.SubChannel
import firelib.domain.OrderState
import java.time.Instant


interface OrderManager {

    /**
     * position
     */
    fun position(): Int

    /**
     * any market order on market or not accepted limit order
     */
    fun hasPendingState(): Boolean

    /**
     * alias of security as configured in model config instruments list
     */
    fun security(): String

    fun submitOrders(orders: List<Order>)

    fun liveOrders(): List<Order>

    fun currentTime() : Instant

    fun tradesTopic() : SubChannel<Trade>

    fun orderStateTopic() : SubChannel<OrderState>

    fun cancelOrders(orders: List<Order>)

    fun nextOrderId (): String

}