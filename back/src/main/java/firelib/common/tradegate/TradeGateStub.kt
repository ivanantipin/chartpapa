package firelib.common.tradegate

import firelib.common.Order
import firelib.common.config.ModelBacktestConfig
import firelib.common.timeservice.TimeService
import firelib.domain.OrderType
import java.time.Instant


class TradeGateStub(val cfg: ModelBacktestConfig, val timeService: TimeService) : TradeGate {

    val limitBooks = cfg.instruments.map {
        BookStub(timeService, LimitOBook())
    }.toTypedArray()

    val stopBooks = cfg.instruments.map {
        BookStub(timeService, StopOBook())
    }.toTypedArray()

    val marketSubs = cfg.instruments.map {
        MarketOrderStub(timeService)
    }.toTypedArray()


    fun updateBidAsks(i : Int, time : Instant, price : Double) {
        val (bid,ask) = cfg.adjustSpread(price,price)
        limitBooks[i].updateBidAsk(bid, ask,time)
        stopBooks[i].updateBidAsk(bid, ask,time)
        marketSubs[i].updateBidAsk(bid, ask,time)
    }


    fun getSecIdx(sec: String): Int {
        return cfg.instruments.indexOfFirst { it.ticker == sec }
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