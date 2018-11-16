package firelib.common.tradegate

import firelib.common.agenda.Agenda
import firelib.common.misc.{DurableChannel, SubChannel}
import firelib.common.timeservice.TimeService
import firelib.common.{Order, Trade}
import firelib.domain.OrderState

class TradeGateDelay(val timeService: TimeService, val delayMillis: Long, val tradeGate: TradeGate, val agenda: Agenda) extends TradeGate {
    /**
    * just order send
    */
    override def sendOrder(order: Order): (SubChannel[Trade], SubChannel[OrderState]) = {
        val trdS = new DurableChannel[Trade]()
        val ordS = new DurableChannel[OrderState]()

        agenda.addEvent(timeService.currentTime.plusMillis(delayMillis), () => {
            val sss: (SubChannel[Trade], SubChannel[OrderState]) = tradeGate.sendOrder(order)
            sss._1.subscribe(t=>trdS.publish(t))
            sss._2.subscribe(o=>ordS.publish(o))
        },0)
        (trdS, ordS)
    }

    /**
     * just order cancel
    */
    override def cancelOrder(order: Order): Unit = {
        agenda.addEvent(timeService.currentTime.plusMillis(delayMillis), () => {
            tradeGate.cancelOrder(order)
        },0)
    }
}
