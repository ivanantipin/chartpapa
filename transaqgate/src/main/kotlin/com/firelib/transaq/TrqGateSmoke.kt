package com.firelib.transaq

import firelib.common.Order
import firelib.core.domain.OrderType
import firelib.core.domain.Side
import firelib.core.store.trqMapperWriter
import java.time.Instant
import java.util.concurrent.Executors

fun main() {

    System.setProperty("env", "prod")

    val stub = makeDefaultStub()

    val msgDispatcher = TrqMsgDispatcher(stub)

    val gate = TrqGate(msgDispatcher, Executors.newSingleThreadExecutor())

    val rasp = trqMapperWriter().read().filter { it.code == "RASP" }.first()

    gate.sendOrder(Order(OrderType.Market, 0.0, 1, Side.Buy, "RASP", "OEUIIeuieu",
        Instant.now(), rasp, "dummy"))

    msgDispatcher.addSync<TrqPortfolio> ({ it is TrqPortfolio }, {
         println(it)
     })

    val resp = msgDispatcher.stub.command(TrqCommandHelper.getPortfolio("1Y65N/1Y65N"))
    println(resp)


    //enableReconnect(msgDispatcher)

}
