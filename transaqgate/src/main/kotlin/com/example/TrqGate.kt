package com.example

import com.example.TrqParser.parseTrqMsg
import com.example.TrqParser.parseTrqResponse
import com.firelib.Empty
import com.firelib.Str
import com.firelib.TransaqConnectorGrpc
import com.google.common.util.concurrent.SettableFuture
import firelib.common.Order
import firelib.common.cancel
import firelib.common.cancelReject
import firelib.common.reject
import firelib.common.tradegate.TradeGate
import org.apache.commons.text.StringEscapeUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor

class TrqGate(val blockingStub: TransaqConnectorGrpc.TransaqConnectorBlockingStub,
              val clientId: String,
              val executor: Executor) : TradeGate {

    fun sendCmd(str: String): TrqResponse {
        return parseTrqResponse(blockingStub.sendCommand(Str.newBuilder().setTxt(str).build()).txt)
    }

    val pendingOrders = ConcurrentHashMap<String, Order>()
    val ordersByOrderNumber = ConcurrentHashMap<String, Order>()


    override fun sendOrder(order: Order) {
        val resp = sendCmd(TrqCommandHelper.newOrder(order, clientId))

        if (!resp.success) {
            order.reject("${resp.message}")
        } else {
            pendingOrders[resp.transactionid!!] = order
        }
    }



    fun processMsg(msg: TrqMsg) {
        when (msg) {
            is TrqOrder -> {
                if (msg.orderno!!.isNotEmpty()) {

                    if (pendingOrders.contains(msg.transactionid)) {
                        ordersByOrderNumber[msg.orderno!!] = pendingOrders[msg.transactionid]!!
                    }
                }
                val order = ordersByOrderNumber[msg.orderno!!]!!

                if (msg.status == "cancelled" && msg.withdrawtime != "0") {
                    order.cancel()
                }


            }
        }
    }

    override fun cancelOrder(order: Order) {
        val transactionid = order.anyInfo["transactionid"]!! as String
        val response = sendCmd(TrqCommandHelper.cancelOrder(transactionid))
        if (!response.success) {
            order.cancelReject("${response.message}")
        }
    }
}


interface TrqMsgListener{
    fun onSec(securities: Securities)
}

class TrqCondMsgListener<T : TrqMsg>(val predicate: (TrqMsg) -> Boolean, val future : SettableFuture<T>){

    fun check(msg : TrqMsg) : Boolean{
        val ret = predicate(msg)
        if(ret){
            future.set(msg as T)
        }
        return ret
    }

}

class MsgCallbacker(val blockingStub: TransaqConnectorGrpc.TransaqConnectorBlockingStub,  executor: Executor){

    val listeners = ConcurrentLinkedQueue<TrqCondMsgListener<out TrqMsg>>()

    init {
        Thread {
            while (true) {
                try {
                    val messages = blockingStub.connect(Empty.newBuilder().build())
                    //continuous messages, this call will generally block till the end
                    messages.forEachRemaining {
                        val msg = parseTrqMsg(StringEscapeUtils.unescapeJava(it.txt))
                        if (msg != null) {
                            executor.execute {
                                listeners.removeIf {
                                    it.check(msg)
                                }
                            }
                        }

                    }
                } catch (e: Exception) {
                    println("error processing ${e}")
                }
            }
        }.start()
    }


    fun <T : TrqMsg> getNext(predicate : (TrqMsg)->Boolean) : SettableFuture<T>{
        val ret = SettableFuture.create<T>()
        listeners.add(TrqCondMsgListener<T>(predicate, ret))
        return ret
    }
}