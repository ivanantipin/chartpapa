package firelib.core.domain

import firelib.common.Order
import java.time.Instant

fun Order.reject(reason: String) {
    this.orderSubscription.publish(OrderState(this, OrderStatus.Rejected, Instant.now(), reason))
}

fun Order.cancelReject(reason: String) {
    this.orderSubscription.publish(OrderState(this, OrderStatus.CancelFailed, Instant.now(), reason))
}


fun Order.cancel() {
    this.orderSubscription.publish(OrderState(this, OrderStatus.Cancelled, Instant.now(), ""))
}

fun Order.done() {
    this.orderSubscription.publish(OrderState(this, OrderStatus.Done, Instant.now(), ""))
}

fun Order.accepted() {
    this.orderSubscription.publish(OrderState(this, OrderStatus.Accepted, Instant.now(), ""))
}

