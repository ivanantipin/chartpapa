package firelib.common.tradegate

import java.util
import java.util.Comparator

import firelib.common.misc.{DurableChannel, SubChannel}
import firelib.common.timeservice.TimeService
import firelib.common.{Order, OrderStatus, Side, Trade}
import firelib.domain.OrderState

abstract class BookStub(val timeService : TimeService) {

    protected var bid,ask = Double.NaN

    def sellOrdering  : Comparator[OrderKey]
    def buyOrdering  : Comparator[OrderKey]

    def buyMatch(bid : Double, ask : Double, ordPrice : Double) : Boolean
    def sellMatch(bid : Double, ask : Double, ordPrice : Double) : Boolean

    def buyPrice(bid : Double, ask : Double, ordPrice : Double) : Double
    def sellPrice(bid : Double, ask : Double, ordPrice : Double) : Double


    def keyForOrder(order : Order) : OrderKey = new OrderKey(order.longPrice, order.id)

    val buyOrders = new util.TreeMap[OrderKey,OrderRec](buyOrdering)

    val sellOrders = new util.TreeMap[OrderKey,OrderRec](sellOrdering)

    /**
     * just order send
     */
    def sendOrder(order: Order): (SubChannel[Trade],SubChannel[OrderState]) = {
        val el = OrderRec (order, new DurableChannel[Trade](),new DurableChannel[OrderState]())
        val ords = if(order.side == Side.Buy) buyOrders else sellOrders
        ords.put(keyForOrder(order),el)
        el.ordSubject.publish(new OrderState(order,OrderStatus.Accepted,timeService.currentTime))
        checkOrders()
        (el.trdSubject,el.ordSubject)
    }


    /**
     * just order cancel
     */
    def cancelOrder(order : Order): Unit = {
        val ords = if(order.side == Side.Buy) buyOrders else sellOrders
        val rec: OrderRec = ords.remove(keyForOrder(order))
        if(rec != null){
            //this can be null due to delay trade gate
            rec.ordSubject.publish(new OrderState(order,OrderStatus.Cancelled,timeService.currentTime))
        }

    }

    def updateBidAsk(bid: Double, ask: Double) = {
        this.bid = bid
        this.ask = ask
        checkOrders()
    }

    def checkOrders() : Unit = {
        checkOrders(buyOrders,buyMatch(bid,ask,_),buyPrice(bid,ask,_))
        checkOrders(sellOrders,sellMatch(bid,ask,_),sellPrice(bid,ask,_))
    }

    def checkOrders(ords : util.TreeMap[OrderKey,OrderRec], matchFunc : Double=>Boolean, priceFunc : Double=>Double) : Unit = {
        val iter = ords.entrySet().iterator()
        var flag = true
        while(iter.hasNext && flag){
            val rec = iter.next().getValue
            if(matchFunc(rec.order.price)){
                iter.remove()
                rec.trdSubject.publish(new Trade(rec.order.qty, priceFunc(rec.order.price), rec.order, timeService.currentTime))
                rec.ordSubject.publish(new OrderState(rec.order, OrderStatus.Done, timeService.currentTime))
            }else{
                flag = false
            }
        }
    }
}
