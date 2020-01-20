package com.firelib.transaq

import com.firelib.Str
import com.firelib.TransaqConnectorGrpc
import com.firelib.transaq.TrqParser.parseTrqResponse
import com.funstat.domain.InstrId
import firelib.common.*
import firelib.common.tradegate.TradeGate
import firelib.domain.OrderType
import firelib.domain.Side
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class TrqGate(val blockingStub: TransaqConnectorGrpc.TransaqConnectorBlockingStub,
              val clientId: String,
              val executor: Executor) : TradeGate {

    fun sendCmd(str: String): TrqResponse {
        return parseTrqResponse(blockingStub.sendCommand(Str.newBuilder().setTxt(str).build()).txt)
    }


    val orderByTransactionId = ConcurrentHashMap<String, Order>()
    val ordersByOrderNumber = ConcurrentHashMap<String, Order>()

    val orderNoToTransactionId = ConcurrentHashMap<String, String>()


    override fun sendOrder(order: Order) {
        val resp = sendCmd(TrqCommandHelper.newOrder(order, clientId))

        if (!resp.success) {
            order.reject("${resp.message}")
        } else {
            orderByTransactionId[resp.transactionid!!] = order
            orderNoToTransactionId[order.id] = resp.transactionid!!
        }
    }

    val callbacker = MsgCallbacker(blockingStub, executor)

    init {
        callbacker.addPlainListener {
            processMsg(it)
        }
    }


    fun getNativeOrder(ord : TrqOrder) : Order?{
        if(orderByTransactionId.containsKey(ord.transactionid)){
            val order = orderByTransactionId[ord.transactionid]!!
            if(!ord.orderno.isNullOrBlank()){
                ordersByOrderNumber[ord.orderno!!] = order
                orderByTransactionId.remove(ord.transactionid)
            }
            return order
        }
        if(ordersByOrderNumber.containsKey(ord.orderno)){
            return ordersByOrderNumber[ord.orderno]!!
        }
        return null
    }


    fun processMsg(msgTrq: TrqMsg) {
        when (msgTrq) {
            is TrqOrders -> {
                msgTrq.orders.forEach { msg->
                    val order = getNativeOrder(msg)
                    if(order == null){
                        println("error, no order found for ${msg} ")
                    }else{
                        if (msg.status == "cancelled" && msg.withdrawtime != "0") {
                            order.cancel()
                        }
                        if (msg.status == "active" ) {
                            order.accepted()
                        }

                        if (msg.status == "matched" ) {
                            order.done()
                        }
                    }

                }
            }
        }
    }

    override fun cancelOrder(order: Order) {
        val transactionid = orderNoToTransactionId[order.id]!!
        val response = sendCmd(TrqCommandHelper.cancelOrder(transactionid))
        if (!response.success) {
            order.cancelReject("${response.message}")
        }
    }
}

fun main() {

    val gate = makeDefaultTransaqGate(Executors.newSingleThreadExecutor())

    val instrId = InstrId(code = "SBER", board = "TQBR", minPriceIncr = BigDecimal(0.1))

    val order = Order(OrderType.Limit, 256.0, 1, Side.Buy, "sber", "0", Instant.now(), instrId)
    order.orderSubscription.subscribe { println(it) }

    gate.sendOrder(order)

    Thread.sleep(10000)

    gate.cancelOrder(order)

}




fun makeDefaultStub() : TransaqConnectorGrpc.TransaqConnectorBlockingStub{
    return TransaqGrpcClientExample("localhost", 50051).blockingStub
}

fun TransaqConnectorGrpc.TransaqConnectorBlockingStub.command(str : String) : TrqResponse {
    return parseTrqResponse(StringEscapeUtils.unescapeJava(this.sendCommand(Str.newBuilder().setTxt(str).build()).txt))
}


fun makeDefaultTransaqGate(executor: Executor): TrqGate {
    val stub = makeDefaultStub()

    println("login response ${stub.command(TrqCommandHelper.connectCmd("TCNN9974", "v9D9z4", "tr1-demo5.finam.ru", "3939"))}")

    return TrqGate(stub, "virt/9974", executor)
}


