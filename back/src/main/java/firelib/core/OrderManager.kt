package firelib.core

import firelib.common.Order
import firelib.common.Trade
import firelib.core.domain.InstrId
import firelib.core.domain.OrderState
import firelib.core.misc.SubChannel
import java.time.Instant


interface OrderManager {

    /**
     * position in lots
     */
    fun position(): Int

    fun positionTime() : Instant

    fun positionPrice() : Double

    /**
     * any market order on market or not accepted limit order
     */
    fun hasPendingState(): Boolean

    /**
     * alias of security as configured in model config instruments list
     */
    fun security(): String

    fun instrument() : InstrId

    fun submitOrders(order : Order)

    fun liveOrders(): List<Order>

    fun currentTime() : Instant

    fun tradesTopic() : SubChannel<Trade>

    fun orderStateTopic() : SubChannel<OrderState>

    fun cancelOrders(orders: Order)

    fun modelName() : String

    fun nextOrderId (): String

    fun updatePosition(pos : Int, time : Instant)

}