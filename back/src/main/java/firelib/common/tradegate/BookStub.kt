package firelib.common.tradegate

import firelib.common.Order
import firelib.common.OrderStatus
import firelib.domain.Side
import firelib.common.Trade
import firelib.common.timeservice.TimeService
import firelib.domain.OrderState
import java.time.Instant
import java.util.*

class BookStub(val timeService : TimeService, val strategy : OrderStrategy) {

    protected var bid = Double.NaN
    protected var ask = Double.NaN
    var priceTime : Instant = Instant.now()

    fun keyForOrder(order : Order) : OrderKey = OrderKey(order.longPrice, order.id)

    val buyOrders = TreeMap<OrderKey,Order>(strategy.buyOrdering())

    val sellOrders = TreeMap<OrderKey,Order>(strategy.sellOrdering())

    /**
     * just order send
     */
    fun sendOrder(order: Order) {
        val ords = if(order.side == Side.Buy) buyOrders else sellOrders
        ords.put(keyForOrder(order),order)
        order.orderSubscription.publish(OrderState(order,OrderStatus.Accepted,timeService.currentTime()))
        checkOrders()
    }


    /**
     * just order cancel
     */
    fun cancelOrder(order : Order): Unit {
        val ords = if(order.side == Side.Buy) buyOrders else sellOrders
        val rec: Order? = ords.remove(keyForOrder(order))
        if(rec != null){
            //this can be null due to delay trade gate
            rec.orderSubscription.publish(OrderState(order,OrderStatus.Cancelled,timeService.currentTime()))
        }

    }

    fun updateBidAsk(bid: Double, ask: Double, first: Instant) {
        this.bid = bid
        this.ask = ask
        this.priceTime = first
        checkOrders()
    }

    fun checkOrders() : Unit {
        checkOrders(buyOrders,{strategy.buyMatch(bid,ask,it)},{strategy.buyPrice(bid,ask,it)})
        checkOrders(sellOrders,{strategy.sellMatch(bid,ask,it)},{strategy.sellPrice(bid,ask,it)})
    }

    fun checkOrders(ords : TreeMap<OrderKey,Order>, whenFunc : (Double)->Boolean, priceFunc : (Double)->Double) : Unit {
        val iter = ords.iterator()
        var flag = true
        while(iter.hasNext() && flag){
            val rec = iter.next().value
            if(whenFunc(rec.price)){
                iter.remove()
                rec.tradeSubscription.publish(Trade(rec.qty, priceFunc(rec.price), rec, timeService.currentTime(),priceTime))
                rec.orderSubscription.publish(OrderState(rec, OrderStatus.Done, timeService.currentTime()))
            }else{
                flag = false
            }
        }
    }
}