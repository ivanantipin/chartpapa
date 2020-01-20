package com.firelib.transaq

import com.firelib.Str
import com.firelib.transaq.TrqCommandHelper.connectCmd
import com.funstat.domain.InstrId
import firelib.common.Order
import firelib.domain.OrderType
import firelib.domain.Side
import org.apache.commons.text.StringEscapeUtils
import java.time.Instant
import java.util.concurrent.Executors

fun main() {

    val gate = makeDefaultTransaqGate(Executors.newSingleThreadExecutor())

    val order = Order(OrderType.Market, 0.0, 1, Side.Buy, "sber", "0", Instant.now(), InstrId(code = "SRH0", board = "FUT"))
    gate.sendOrder(order)
    order.orderSubscription.subscribe { println(it) }

}



