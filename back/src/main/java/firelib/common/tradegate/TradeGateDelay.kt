package firelib.common.tradegate

import firelib.common.Order
import firelib.common.agenda.Agenda
import firelib.common.timeservice.TimeService

class TradeGateDelay(val timeService: TimeService, val delayMillis: Long, val tradeGate: TradeGate, val agenda: Agenda) : TradeGate {
    /**
     * just order send
     */
    override fun sendOrder(order: Order){
        agenda.execute(timeService.currentTime().plusMillis(delayMillis), {
            tradeGate.sendOrder(order)
        }, 0)
    }

    /**
     * just order cancel
     */
    override fun cancelOrder(order: Order): Unit {
        agenda.execute(timeService.currentTime().plusMillis(delayMillis), {
            tradeGate.cancelOrder(order)
        }, 0)
    }
}