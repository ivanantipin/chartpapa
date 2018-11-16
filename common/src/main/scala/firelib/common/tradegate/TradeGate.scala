package firelib.common.tradegate

import firelib.common.misc.SubChannel
import firelib.common.{Order, Trade}
import firelib.domain.OrderState


/**
 * Main interface for execution to adapt broker api.
 */
trait TradeGate {
    /**
     * just order send
     */
    def sendOrder(order: Order) : (SubChannel[Trade],SubChannel[OrderState])
    /**
     * order cancel
     */
    def cancelOrder(order: Order)
}