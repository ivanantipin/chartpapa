package firelib.core.backtest.tradegate

import firelib.common.Order
import firelib.core.TradeGate
import firelib.core.domain.OrderType
import firelib.core.timeservice.TimeService
import java.time.Instant


class TradeGateStub(val instruments: List<String>, val timeService: TimeService,
                    val bidAdjuster: (Double) -> Double = { price : Double -> price},
                    val askAdjuster: (Double) -> Double = { price : Double -> price}
) :
    TradeGate {

    val tickerToIndex = instruments.mapIndexed({idx, tick-> tick to idx}).toMap()

    val limitBooks = instruments.map {
        BookStub(timeService, LimitOBook())
    }.toTypedArray()

    val stopBooks = instruments.map {
        BookStub(timeService, StopOBook())
    }.toTypedArray()

    val marketSubs = instruments.map {
        MarketOrderStub(timeService)
    }.toTypedArray()


    fun updateBidAsks(i: Int, time: Instant, price: Double) {
        val bid = bidAdjuster(price)
        val ask = askAdjuster(price)
        limitBooks[i].updateBidAsk(bid, ask, time)
        stopBooks[i].updateBidAsk(bid, ask, time)
        marketSubs[i].updateBidAsk(bid, ask, time)
    }

    /**
     * just order send
     */
    override fun sendOrder(order: Order) {
        val secIdx = tickerToIndex[order.security]!!
        when (order.orderType) {
            OrderType.Limit -> limitBooks[secIdx].sendOrder(order)
            OrderType.Stop -> stopBooks[secIdx].sendOrder(order)
            OrderType.Market -> marketSubs[secIdx].sendOrder(order)
        }
    }

    /**
     * just order cancel
     */
    override fun cancelOrder(order: Order) {
        val secIdx = tickerToIndex[order.security]!!
        when (order.orderType) {
            OrderType.Limit -> limitBooks[secIdx].cancelOrder(order)
            OrderType.Stop -> stopBooks[secIdx].cancelOrder(order)
            OrderType.Market -> throw RuntimeException("not possible to cancel market order ${order}")
        }
    }
}


