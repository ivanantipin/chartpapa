package com.firelib.prod

import com.firelib.transaq.*
import firelib.common.Order
import firelib.core.domain.Interval
import firelib.core.domain.OrderType
import firelib.core.domain.Side
import firelib.core.store.DbMapper
import firelib.core.store.trqMapperWriter
import java.time.Instant
import java.util.concurrent.Executors

fun main() {
//    val symbols = TrqHistoricalSource(makeDefaultStub(), "1").symbols()
//
//    trqMapperWriter().write(symbols)
//
//    println("count is ${symbols.size}")

    System.setProperty("env", "prod")




    val dbMapper = DbMapper(trqMapperWriter(), { true })

    println(dbMapper("sber"))

    val stub = makeDefaultStub()

    stub.command(loginCmd)

    val dist = TrqMsgDispatcher(stub)

    val gate = TrqGate(dist, Executors.newSingleThreadExecutor(), "1Y65S/1Y65S")

    enableMsgLogging(dist, {it is Markets || it is TrqClient})

    stub.command(TrqCommandHelper.markets())

    val order = Order(OrderType.Market, 0.0, 1, Side.Buy, "eurusd", "idd1", Instant.now(), dbMapper("aapl"))
    gate.sendOrder(order)
    order.orderSubscription.subscribe {
        println(it)
    }
    order.tradeSubscription.subscribe {
        println(it)
    }







//    stub.command(TrqCommandHelper.disconnectCmd())


    stub.command(TrqCommandHelper.markets())






}