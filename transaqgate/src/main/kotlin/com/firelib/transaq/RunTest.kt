package com.firelib.transaq

import firelib.common.Order
import firelib.core.domain.InstrId
import firelib.core.domain.OrderType
import firelib.core.domain.Side
import java.time.Instant
import java.util.concurrent.Executors

fun main() {

    val gate = makeDefaultTransaqGate(Executors.newSingleThreadExecutor())

    val order = Order(OrderType.Market, 0.0, 1, Side.Buy, "sber", "0", Instant.now(),
        InstrId(code = "SRH0", board = "FUT")
    )
    gate.sendOrder(order)
    order.orderSubscription.subscribe { println(it) }

}



