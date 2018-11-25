package firelib.domain

import java.time.Instant

import firelib.common.Order
import firelib.common.OrderStatus

data class OrderState(val order : Order, val status : OrderStatus, val time : Instant, val msg : String? = null)