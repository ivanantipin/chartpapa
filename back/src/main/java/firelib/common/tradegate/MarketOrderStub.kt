package firelib.common.tradegate

import firelib.common.Order
import firelib.common.OrderStatus
import firelib.common.Side
import firelib.common.Trade
import firelib.common.timeservice.TimeService
import firelib.domain.OrderState


class MarketOrderStub(val timeService : TimeService,var bid : Double = Double.NaN,
                      var ask : Double = Double.NaN) {



    fun price(side : Side) : Double = if(side == Side.Sell) bid else ask

    /**
     * just order send
     */
    fun sendOrder(order: Order){
        val trdPrice: Double = price(order.side)

        if(trdPrice.isNaN()){
            order.orderSubscription.publish(OrderState(order, OrderStatus.Rejected, timeService.currentTime()))
        }else{
            order.tradeSubscription.publish(Trade(order.qty, trdPrice, order, timeService.currentTime()))
            order.orderSubscription.publish(OrderState(order, OrderStatus.Done, timeService.currentTime()))
        }
    }


    fun updateBidAsk(bid: Double, ask: Double) {
        this.bid = bid
        this.ask = ask
    }
}