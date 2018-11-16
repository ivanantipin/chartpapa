package firelib.domain

import firelib.common.misc.SubChannel
import firelib.common.{Order, OrderStatus, Trade}

import scala.collection.mutable.ArrayBuffer

class OrderWithState(val order : Order) {

    def status = statuses.last
    val trades = new ArrayBuffer[Trade]()
    val statuses = OrderStatus.New +: new ArrayBuffer[OrderStatus]()

    def remainingQty : Int = order.qty - trades.map(_.qty).sum

    var tradeSubscription : SubChannel[Trade] =_

    var orderSubscription : SubChannel[OrderState] =_

}
