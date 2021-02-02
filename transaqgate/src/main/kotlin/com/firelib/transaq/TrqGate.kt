package com.firelib.transaq

import com.firelib.Str
import com.firelib.TransaqConnectorGrpc
import com.firelib.transaq.TrqParser.parseTrqResponse
import firelib.common.Order
import firelib.common.Trade
import firelib.core.TradeGate
import firelib.core.domain.*
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants
import io.grpc.Deadline
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit


val clientsDao = GeGeWriter<TrqClientDb>(GlobalConstants.metaDb, TrqClientDb::class, listOf("id"), "trq_clients")

data class TrqClientDb(
    val id : String,
    var market: String,
    var currency: String,
    var type: String,
    var union_trd : String
)

class TrqGate(
    val dispatcherTrq: TrqMsgDispatcher,
    val executor: Executor
) : TradeGate {
    private val log = LoggerFactory.getLogger(javaClass)

    val orderByTransactionId = ConcurrentHashMap<String, Order>()
    val ordersByOrderNumber = ConcurrentHashMap<String, Order>()

    val orderNoToTransactionId = ConcurrentHashMap<String, String>()

    val union: String

    override fun sendOrder(order: Order) {

        try {
            val resp = dispatcherTrq.stub.command(TrqCommandHelper.newOrder(order, union))
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

        val clId = GlobalConstants.props.get("trq.client.id")

        log.info("using client override ${clId}")

        union = clId ?: clientsDao.read().filter { it.market == "1" }[0].union_trd

        log.info("client id set to ${union}")
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


fun makeDefaultStub(): TransaqConnectorGrpc.TransaqConnectorBlockingStub {
    return TransaqClient(GlobalConstants.getProp("stub.ip"), GlobalConstants.getProp("stub.port").toInt()).blockingStub
}

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

val loginCmd = TrqCommandHelper.connectCmd(
    GlobalConstants.getProp("login"),
    GlobalConstants.getProp("password"),
    GlobalConstants.getProp("host"),
    GlobalConstants.getProp("port")
)

fun makeDefaultTransaqGate(executor: Executor): TrqGate {
    val stub = makeDefaultStub()
    return TrqGate(TrqMsgDispatcher(stub), executor)
}


