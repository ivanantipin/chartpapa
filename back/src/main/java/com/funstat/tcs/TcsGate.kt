package com.funstat.tcs

import firelib.common.Order
import firelib.common.core.TcsTickerMapper
import firelib.common.tradegate.TradeGate
import firelib.domain.OrderState
import firelib.domain.Side
import ru.tinkoff.invest.openapi.data.LimitOrder
import ru.tinkoff.invest.openapi.data.OperationType
import ru.tinkoff.invest.openapi.data.OrderStatus
import ru.tinkoff.invest.openapi.data.StreamingEvent
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Flow

class TcsGate(val executor: ExecutorService, val mapper : TcsTickerMapper) : TradeGate, Flow.Subscriber<StreamingEvent> {

    var subscription: Flow.Subscription? = null

    init {
        mapper.context.subscribe(this)
    }

    override fun onComplete() {
        println("completed")
    }

    override fun onSubscribe(subscription: Flow.Subscription) {
        subscription.request(Long.MAX_VALUE)
        this.subscription = subscription
    }

    override fun onNext(event: StreamingEvent) {


    }

    override fun onError(p0: Throwable?) {
        println("error ${p0}")
    }

    override fun cancelOrder(order: Order) {
        mapper.context.cancelOrder(order.id)
    }

    override fun sendOrder(order: Order) {
        val opType = if (order.side == Side.Buy) OperationType.Buy else OperationType.Sell

        val instr = mapper.map(order.security)!!

        val price = instr.minPriceIncr.multiply(order.price.toBigDecimal().divide(instr.minPriceIncr).toInt().toBigDecimal())

        val future = mapper.context.placeLimitOrder(LimitOrder(instr.id, order.qty/instr.lot, opType, price))

        future.thenAccept({
            executor.run {
                when (it.status) {
                    OrderStatus.Rejected -> order.orderSubscription.publish(OrderState(order,
                            firelib.common.OrderStatus.Cancelled, Instant.now(), it.rejectReason))
                    OrderStatus.New -> order.orderSubscription.publish(OrderState(order,
                            firelib.common.OrderStatus.Accepted, Instant.now()))
                }
                println("order status is ${it}")
            }
        }).exceptionally {
            order.orderSubscription.publish(OrderState(order,
                    firelib.common.OrderStatus.Cancelled, Instant.now(), "${it.message}"))
            null
        }
    }
}