package firelib.common.tradegate

import firelib.common.misc.Channel
import firelib.common.{Order, Trade}
import firelib.domain.OrderState

case class OrderRec (val order : Order, val trdSubject : Channel[Trade], val ordSubject : Channel[OrderState])
