package firelib.domain

import firelib.common.Order
import java.time.Instant

data class OrderState(val order : Order, val status : OrderStatus, val time : Instant, val msg : String? = null)