package firelib.common.tradegate

import firelib.common.Order
import firelib.domain.OrderStatus
import firelib.common.Trade
import firelib.common.timeservice.TimeService
import firelib.domain.OrderState
import firelib.domain.Side
import java.time.Instant


class MarketOrderStub(val timeService: TimeService, var bid: Double = Double.NaN,
                      var ask: Double = Double.NaN,
                      var priceTime: Instant = Instant.now()
) {


    fun price(side: Side): Double = if (side == Side.Sell) bid else ask

    /**
     * just order send
     */
    fun sendOrder(order: Order) {
        val trdPrice: Double = price(order.side)

        if (trdPrice.isNaN()) {
            order.orderSubscription.publish(OrderState(order, OrderStatus.Rejected, timeService.currentTime()))
        } else {
            order.tradeSubscription.publish(Trade(order.qtyLots, trdPrice, order, timeService.currentTime(), priceTime!!))
            order.orderSubscription.publish(OrderState(order, OrderStatus.Done, timeService.currentTime()))
        }
    }


    fun updateBidAsk(bid: Double, ask: Double, priceTime: Instant) {
        this.bid = bid
        this.ask = ask
        this.priceTime = priceTime
    }
}