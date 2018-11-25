package firelib.common.tradegate

import firelib.common.misc.SubChannel
import firelib.common.Order
import firelib.common.Trade
import firelib.domain.OrderState


/**
 * Main interface for execution to adapt broker api.
 */
interface TradeGate {
    /**
     * just order send
     */
    fun sendOrder(order: Order)
    /**
     * order cancel
     */
    fun cancelOrder(order: Order)
}