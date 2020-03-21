package com.firelib.transaq

import com.firelib.Str
import com.firelib.TransaqConnectorGrpc
import com.firelib.transaq.TrqParser.parseTrqResponse
import firelib.common.Order
import firelib.common.Trade
import firelib.core.TradeGate
import firelib.core.domain.*
import firelib.core.store.GlobalConstants
import io.grpc.Deadline
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TrqGate(
    val dispatcherTrq: TrqMsgDispatcher,
    val executor: Executor,
    var clientId: String
) : TradeGate {

    private val log = LoggerFactory.getLogger(javaClass)

    val orderByTransactionId = ConcurrentHashMap<String, Order>()
    val ordersByOrderNumber = ConcurrentHashMap<String, Order>()

    val orderNoToTransactionId = ConcurrentHashMap<String, String>()


    override fun sendOrder(order: Order) {

        try {
            val resp = dispatcherTrq.stub.command(TrqCommandHelper.newOrder(order, clientId))
            if (!resp.success) {
                order.reject("${resp.message}")
            } else {
                orderByTransactionId[resp.transactionid!!] = order
                orderNoToTransactionId[order.id] = resp.transactionid!!
            }
        } catch (e: Exception) {
            order.reject("uncknown error ${e.message}")
        }
    }

    init {
        dispatcherTrq.addSync<TrqMsg>({ true }, { msg ->
            executor.execute({ processMsg(msg) })
        })
    }


    fun getNativeOrder(ord: TrqOrder): Order? {
        if (orderByTransactionId.containsKey(ord.transactionid)) {
            val order = orderByTransactionId[ord.transactionid]!!
            if (!ord.orderno.isNullOrBlank()) {
                ordersByOrderNumber[ord.orderno!!] = order
            }
            log.info("got order by transaction id ${ord.transactionid}")
            return order
        }
        if (ordersByOrderNumber.containsKey(ord.orderno)) {
            log.info("got order by orderno ${ord.orderno}")
            return ordersByOrderNumber[ord.orderno]!!
        }
        return null
    }


    fun processMsg(msgTrq: TrqMsg) {
        when (msgTrq) {
            is TrqOrders -> {
                msgTrq.orders.forEach { msg ->
                    val order = getNativeOrder(msg)
                    if (order == null) {
                        log.error("error, no order found for ${msg} ")
                    } else {
                        if (msg.status == "cancelled" && msg.withdrawtime != "0") {
                            order.cancel()
                        } else if (msg.status == "active") {
                            order.accepted()
                        } else if (msg.status == "matched") {
                            order.done()
                        } else {
                            log.error("can not process status ${msg}")
                        }
                    }

                }
            }
            is AllTrades -> {
            }
            is TrqClient -> {
                if(msgTrq.market == "1"){
                    log.info("client  received ${msgTrq}")
                    clientId = msgTrq.id!!
                }
            }
            is TrqTrades -> {
                msgTrq.trades.forEach { trade ->
                    val order = ordersByOrderNumber.get(trade.orderno)
                    if (order == null) {
                        log.error("error received trade for missing order ${trade}")
                    } else {

                        order.tradeSubscription.publish(
                            Trade(
                                tradeNo = trade.tradeno!!,
                                qty = trade.quantity!!.toInt(),
                                price = trade.price!!.toDouble(),
                                order = order,
                                dtGmt = Instant.now(), // fixme
                                priceTime = Instant.now()
                            )
                        )
                    }
                }
            }
        }
    }

    override fun cancelOrder(order: Order) {
        val transactionid = orderNoToTransactionId[order.id]!!
        try {
            val response = dispatcherTrq.stub.command(TrqCommandHelper.cancelOrder(transactionid))
            if (!response.success) {
                order.cancelReject("${response.message}")
            }
        } catch (e: Exception) {
            order.reject("unknown error ${e.message}")
        }

    }
}

fun main() {

    val gate = makeDefaultTransaqGate(Executors.newSingleThreadExecutor())

    val instrId =
        InstrId(code = "SBER", board = "TQBR", minPriceIncr = BigDecimal(0.1))

    val order = Order(OrderType.Limit, 256.0, 1, Side.Buy, "sber", "0", Instant.now(), instrId)
    order.orderSubscription.subscribe { println(it) }

    gate.sendOrder(order)

    Thread.sleep(10000)

    gate.cancelOrder(order)

}


fun makeDefaultStub(): TransaqConnectorGrpc.TransaqConnectorBlockingStub {
    return TransaqGrpcClientExample("localhost", 50052).blockingStub
}

val logger = LoggerFactory.getLogger("command")

fun TransaqConnectorGrpc.TransaqConnectorBlockingStub.command(str: String): TrqResponse {
    return parseTrqResponse(
        StringEscapeUtils.unescapeJava(
            this.withDeadline(
                Deadline.after(
                    3000,
                    TimeUnit.MILLISECONDS
                )
            ).sendCommand(Str.newBuilder().setTxt(str).build()).txt
        )
    )
}


/*
Ваш логин:
Ваш пароль: suV7gU
 */
val loginCmd = TrqCommandHelper.connectCmd(
    GlobalConstants.getProp("login"),
    GlobalConstants.getProp("password"),
    GlobalConstants.getProp("host"),
    GlobalConstants.getProp("port")
)
//val loginCmd = TrqCommandHelper.connectCmd("FBTC277A", "x8Er8EuU", "tr1.finambank.ru", "3324")


fun makeDefaultTransaqGate(executor: Executor): TrqGate {
    val stub = makeDefaultStub()
    return TrqGate(TrqMsgDispatcher(stub), executor, "virt/9952")
}


