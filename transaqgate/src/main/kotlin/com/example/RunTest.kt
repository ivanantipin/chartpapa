package com.example

import com.example.TrqCommandHelper.connectCmd
import com.example.TrqParser.parseTrqMsg
import com.firelib.Empty
import com.firelib.Str
import com.funstat.domain.InstrId
import firelib.common.Order
import firelib.domain.OrderType
import firelib.domain.Side
import org.apache.commons.text.StringEscapeUtils
import java.time.Instant
import java.util.concurrent.Executors

fun main() {

    val client = TransaqGrpcClientExample("localhost", 50051)

    val blockingStub = client.blockingStub

    fun sendCmd(str: String): String {
        return StringEscapeUtils.unescapeJava(blockingStub.sendCommand(Str.newBuilder().setTxt(str).build()).txt)
    }

    println("login response ${sendCmd(connectCmd("TCNN9974", "v9D9z4", "tr1-demo5.finam.ru", "3939"))}")

    val executor = Executors.newSingleThreadExecutor()

    val gate = TrqGate(blockingStub, "virt/9974", executor)

    executor.execute {
        val order = Order(OrderType.Market, 0.0, 1, Side.Buy, "sber", "0", Instant.now(), InstrId(code = "SRH0", board = "FUT"))
        gate.sendOrder(order)
        order.orderSubscription.subscribe { println(it) }
    }

    Thread {
        val messages = blockingStub.connect(Empty.newBuilder().build())
        //continuous messages, this call will generally block till the end
        messages.forEachRemaining { str: Str -> println("server message" + parseTrqMsg(StringEscapeUtils.unescapeJava(str.txt))) }
    }.start()

}


