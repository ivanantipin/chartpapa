package firelib.common.ordermanager

import firelib.domain.InstrId
import firelib.common.Order
import firelib.domain.OrderStatus
import firelib.common.Trade
import firelib.common.misc.NonDurableChannel
import firelib.common.misc.SubChannel
import firelib.common.timeservice.TimeService
import firelib.common.tradegate.TradeGate
import firelib.domain.OrderState
import firelib.domain.OrderType
import firelib.domain.OrderWithState
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.set


class OrderManagerImpl(val tradeGate : TradeGate,
                       val timeService : TimeService,
                       val security : String,
                       val maxOrderCount: Int = 20,
                       val instrument : InstrId
) : OrderManager {

    private val log = LoggerFactory.getLogger(javaClass)


    override fun currentTime(): Instant {
        return timeService.currentTime()
    }

    override fun instrument(): InstrId {
        return instrument
    }

    override fun security(): String {
        return security
    }

    private val id2Order = mutableMapOf<String,OrderWithState>()

    private val orderStateChannel = NonDurableChannel<OrderState>()

    private val tradesChannel = NonDurableChannel<Trade>()

    private var position = 0

    override fun position(): Int = position

    var idCounter = AtomicLong(0)

    override fun liveOrders(): List<Order>{
        return  id2Order.values.map({it.order})
    }

    override fun hasPendingState(): Boolean {
        return id2Order.values.any { (it.status().isPending() || (it.order.orderType == OrderType.Market))}
    }

    override fun tradesTopic(): SubChannel<Trade> {
        return tradesChannel
    }
    override fun orderStateTopic(): SubChannel<OrderState> {
        return orderStateChannel
    }

    override fun cancelOrders(orders: List<Order>) {
        for (order in orders) {
            val ord = id2Order.get(order.id)
            if(ord != null){
                tradeGate.cancelOrder(order)
                ord.statuses += OrderStatus.PendingCancel
                orderStateChannel.publish(OrderState(ord.order, OrderStatus.PendingCancel, timeService.currentTime()))
            }else{
                log.error("cancelling non existing order {}", order)
            }
        }
    }


    override fun submitOrders(orders: List<Order>) {
        if(this.id2Order.size > maxOrderCount){
            log.error("max order count reached rejecting orders {}", orders)
            orders.forEach({orderStateChannel.publish(OrderState(it, OrderStatus.Rejected, timeService.currentTime()))})
        }else{
            orders.forEach { order ->
                val orderWithState = OrderWithState(order)
                this.id2Order[order.id] = orderWithState
                orders.forEach {orderStateChannel.publish(OrderState(it, OrderStatus.New, timeService.currentTime()))}
                log.info("submitting order {}", order)
                order.tradeSubscription.subscribe {onTrade(it, orderWithState)}
                order.orderSubscription.subscribe {onOrderState(it)}
                tradeGate.sendOrder(order)
            }
        }
    }



    fun onOrderState(state : OrderState) {
        if (!id2Order.contains(state.order.id)) {
            log.error("order state received {} for nonexisting or finalized order", state)
            return
        }
        log.info("order state received {} ", state)

        val sOrder: OrderWithState = id2Order[state.order.id]!!

        sOrder.statuses += state.status

        if (state.status.isFinal()) {
            if(state.status == OrderStatus.Done && sOrder.remainingQty() > 0){
                log.error("status is Done but order {} has non zero remaining amount {} leaving in pending... ", state.order, sOrder.remainingQty())
            }else{
                id2Order.remove(state.order.id)
            }

        }
        orderStateChannel.publish(state)
    }


    fun onTrade(trd: Trade, order : OrderWithState) {
        println("on trade ${trd}")
        order.trades += trd
        if(order.remainingQty() < 0){
            println("negative remaining amount order $order")
        }
        if(order.remainingQty() == 0 && order.status().isFinal()){
            println("removing filled order as remaining qty is zero")
            id2Order.remove(order.order.id)
        }
        val prevPos = position
        position = trd.adjustPositionByThisTrade(position)
        println("position adjusted for security $security :  $prevPos -> ${position()}")
        tradesChannel.publish(trd.copy(positionAfter = position))
    }


    override fun nextOrderId(): String {
        return "${security}_${idCounter.incrementAndGet()}"
    }
}