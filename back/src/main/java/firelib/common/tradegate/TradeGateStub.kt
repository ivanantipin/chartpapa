package firelib.common.tradegate

import firelib.common.Order
import firelib.common.OrderType
import firelib.common.config.ModelBacktestConfig
import firelib.common.timeservice.TimeService


class TradeGateStub(val modelConfig: ModelBacktestConfig, val timeService: TimeService) : TradeGate {

    val limitBooks = modelConfig.instruments.map {
        BookStub(timeService, LimitOBook())
    }.toTypedArray()

    val stopBooks = modelConfig.instruments.map {
        BookStub(timeService, StopOBook())
    }.toTypedArray()

    val marketSubs = modelConfig.instruments.map {
        MarketOrderStub(timeService)
    }.toTypedArray()


    fun updateBidAsks(prices: List<Double>) {
        for (i in 0 until marketSubs.size) {
            val bid = prices[i] - 0.005
            val ask = prices[i] + 0.005
            limitBooks[i].updateBidAsk(bid, ask)
            stopBooks[i].updateBidAsk(bid, ask)
            marketSubs[i].updateBidAsk(bid, ask)
        }
    }

    fun getSecIdx(sec: String): Int {
        return modelConfig.instruments.indexOfFirst { it.ticker == sec }
    }

    /**
     * just order send
     */
    override fun sendOrder(order: Order) {
        val secIdx = getSecIdx(order.security)
        when (order.orderType) {
            OrderType.Limit -> limitBooks[secIdx].sendOrder(order)
            OrderType.Stop -> stopBooks[secIdx].sendOrder(order)
            OrderType.Market -> marketSubs[secIdx].sendOrder(order)
        }
    }

    /**
     * just order cancel
     */
    override fun cancelOrder(order: Order): Unit {
        val secIdx = getSecIdx(order.security)
        when (order.orderType) {
            OrderType.Limit -> limitBooks[secIdx].cancelOrder(order)
            OrderType.Stop -> stopBooks[secIdx].cancelOrder(order)
            OrderType.Market -> throw RuntimeException("not possible to cancel market order ${order}")
        }

    }
}