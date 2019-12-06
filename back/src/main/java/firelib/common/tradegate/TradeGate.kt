package firelib.common.tradegate

import firelib.common.Order


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