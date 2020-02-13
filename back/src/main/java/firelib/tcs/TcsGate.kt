package firelib.tcs

import firelib.core.domain.InstrId
import com.google.common.collect.Sets
import firelib.common.Order
import firelib.common.Trade
import firelib.core.misc.moscowZoneId
import firelib.core.TradeGate
import firelib.core.domain.OrderState
import firelib.core.domain.OrderType
import firelib.core.domain.Side
import org.slf4j.LoggerFactory
import ru.tinkoff.invest.openapi.data.*
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.*

class TcsGate(val executor: ExecutorService, val mapper: TcsTickerMapper) : TradeGate, Flow.Subscriber<StreamingEvent> {

    var subscription: Flow.Subscription? = null

    var scheduled: ScheduledExecutorService

    val log = LoggerFactory.getLogger(javaClass)

    val requestedInstrIds : MutableSet<InstrId> = Sets.newConcurrentHashSet<InstrId>()

    val ops = ConcurrentHashMap<String,Operation>()

    val positions = ConcurrentHashMap<InstrId,Long>()


    init {
        mapper.context.subscribe(this)
        this.scheduled = Executors.newScheduledThreadPool(1)

        retrievePositions(true)

        this.scheduled.scheduleAtFixedRate({
            checkOrders()
        },0, 20, TimeUnit.SECONDS)
    }


    fun checkOrders(){

        retrievePositions(false)

//        requestedInstrIds.forEach{ instr->
//
//            val startTime = OffsetDateTime.now(moscowZoneId).minusHours(1)
//            val endTime = OffsetDateTime.now(moscowZoneId)
//
//            mapper.source.context.getOperations(startTime, endTime,instr.id ).join().operations.forEach {op->
//
//
//
//        }
//        }



    }

    private fun retrievePositions(init : Boolean) {

        mapper.context.portfolio.join().positions.forEach { position ->
            val instrId = mapper.map(position.ticker)!!
            val prev = positions.put(instrId, position.lots.toLong())

            if (prev == null || prev != position.lots.toLong()) {
                if(init){
                    log.info("position at start for ${instrId.code} is ${position.lots}")
                }else{
                    log.info("position changed for ${instrId.code} from ${prev} to ${position.lots}")
                }

            }

        }
    }

    override fun onComplete() {
        log.info("completed")
    }



    override fun onSubscribe(subscription: Flow.Subscription) {
        subscription.request(Long.MAX_VALUE)
        this.subscription = subscription
    }

    override fun onNext(event: StreamingEvent) {


    }

    override fun onError(p0: Throwable?) {
        log.info("error ${p0}")
    }

    override fun cancelOrder(order: Order) {
        mapper.context.cancelOrder(order.id)
    }

    val orders = ConcurrentHashMap<String,Order>()

    override fun sendOrder(order: Order) {
        val opType = if (order.side == Side.Buy) OperationType.Buy else OperationType.Sell


        requestedInstrIds += order.instr

        val future = mapper.context.placeLimitOrder(LimitOrder(order.instr.id, order.qtyLots, opType, order.price.toBigDecimal()))

        future.thenAccept({
            executor.run {
                when (it.status) {
                    OrderStatus.Rejected -> order.orderSubscription.publish(OrderState(order,
                            firelib.core.domain.OrderStatus.Cancelled, Instant.now(), it.rejectReason))
                    OrderStatus.New , OrderStatus.Fill, OrderStatus.PartiallyFill -> {
                        if(it.executedLots > 0){
                            order.tradeSubscription.publish(Trade(it.executedLots,order.price,order,Instant.now(),Instant.now()))
                        }
                    }
                }


                log.info("order status is ${it.str()}")
            }
        }).exceptionally {
            order.orderSubscription.publish(OrderState(order,
                    firelib.core.domain.OrderStatus.Cancelled, Instant.now(), "${it.message}"))
            null
        }
        future.get()
    }
}

fun PlacedLimitOrder.str() : String{
    return "PlacedLimitOrder{" +
            "id='" + id + '\'' +
            ", operation=" + operation +
            ", status=" + status +
            ", rejectReason='" + rejectReason + '\'' +
            ", requestedLots=" + requestedLots +
            ", executedLots=" + executedLots +
            ", commission=" + commission +
            ", figi='" + figi + '\'' +
            '}'
}

fun ru.tinkoff.invest.openapi.data.Order.str() : String {
    return "Order{" +
            "id='" + id + '\'' +
            ", figi='" + figi + '\'' +
            ", operation=" + operation +
            ", status=" + status +
            ", requestedLots=" + requestedLots +
            ", executedLots=" + executedLots +
            ", type=" + type +
            ", price=" + price +
            '}'
}

fun Portfolio.str() : String{
    return "Portfolio{" +
            "positions=" + positions +
            '}'
}

//9cbf4c51-f16c-4d49-a190-f09b0e12c3ca
fun main(){


    val mapper = TcsTickerMapper()
    val gate = TcsGate(Executors.newSingleThreadExecutor(), mapper)

    val instr = mapper.map("sber")!!


    val order = Order(OrderType.Limit, 250.0, 10, Side.Buy, "sber", "00", Instant.now(), instr)
    gate.sendOrder(order)

    order.tradeSubscription.subscribe { state->
        println(state)
    }

    println("orders ${mapper.context.orders.join()}")


    mapper.context.getOperations(OffsetDateTime.now(moscowZoneId).minusHours(1), OffsetDateTime.now(moscowZoneId), "BBG004730N88" ).join().operations.forEach(
            {
                println(it)
            }
    )


    order.orderSubscription.subscribe { state->
        println(state)
    }

}