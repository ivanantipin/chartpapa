package com.funstat.tcs

import com.funstat.domain.InstrId
import firelib.common.core.Source
import firelib.common.Order
import firelib.common.core.TcsTickerMapper
import firelib.common.interval.Interval
import firelib.common.misc.moscowZoneId
import firelib.common.tradegate.TradeGate
import firelib.domain.Ohlc
import firelib.domain.OrderState
import firelib.domain.Side
import ru.tinkoff.invest.openapi.data.*
import ru.tinkoff.invest.openapi.wrapper.Context
import ru.tinkoff.invest.openapi.wrapper.impl.ConnectionFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.ExecutorService
import java.util.concurrent.Flow
import java.util.logging.Logger


fun getContext(): Context {
    val connect = ConnectionFactory.connectSandbox("t.OdqOG3D8_OaPqBn-m1wAD4uFYGWBK-1Em4VR_4te1es4PYjMaO288rF8IQ854J0cRpd3DhDCLPHuKi1mDFzpfw", Logger.getGlobal())
    return connect.get().context()
}

class TcsSource(val context: Context) : Source {

    override fun symbols(): List<InstrId> {
        return context.marketStocks.thenApply {
            it.instruments.map { inst ->
                InstrId(id = inst.figi, source = "TCS", name = inst.name, code = inst.ticker)
            }
        }.join()
    }

    override fun load(instrId: InstrId): Sequence<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(300))
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime?): Sequence<Ohlc> {

        var dt = OffsetDateTime.of(dateTime, ZoneOffset.UTC)
        return sequence({

            while (dt < OffsetDateTime.now()) {
                val candles = context.getMarketCandles(instrId.id,
                        dt, dt.plusDays(1), CandleInterval.ONE_MIN).join()
                yieldAll(candles.candles.map {
                    Ohlc(
                            open = it.o.toDouble(),
                            high = it.h.toDouble(),
                            low = it.l.toDouble(),
                            close = it.c.toDouble(),
                            volume = it.v.toLong(),
                            endTime = it.time.toInstant(),
                            interpolated = false

                    )
                })
                dt = dt.plusDays(1)
            }
        })
    }

    override fun getName(): String {

        moscowZoneId
        return "TCS"
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Day
    }
}

class TcsGate(val ctx: Context, val executor: ExecutorService) : TradeGate, Flow.Subscriber<StreamingEvent> {

    private var instrumentsMap: Map<String, InstrId>
    var subscription: Flow.Subscription? = null


    init {
        instrumentsMap = TcsSource(ctx).symbols().associateBy { it.code }
        subbb()
        println("subscribed")
    }

    fun subbb() {
        ctx.subscribe(this)
    }


    override fun onComplete() {
        println("completed")
    }

    override fun onSubscribe(subscription: Flow.Subscription) {
        this.subscription = subscription
        println("on subs")
    }

    override fun onNext(event: StreamingEvent) {
        println("on next ${event}")
    }

    override fun onError(p0: Throwable?) {
        println("error ${p0}")
    }

    override fun cancelOrder(order: Order) {
        ctx.cancelOrder(order.id)
    }

    override fun sendOrder(order: Order) {
        val opType = if (order.side == Side.Buy) OperationType.Buy else OperationType.Sell

        val instr = instrumentsMap[order.security]!!

        val future = ctx.placeLimitOrder(LimitOrder(instr.id, order.qty, opType, order.price.toBigDecimal()))

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
            println(it)
            null
        }
    }
}