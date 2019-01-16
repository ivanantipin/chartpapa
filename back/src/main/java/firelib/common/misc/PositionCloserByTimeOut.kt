package firelib.common.misc

import firelib.common.Trade
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.makePositionEqualsTo
import java.time.Duration
import java.time.Instant

class PositionCloserByTimeOut(val stub: OrderManager, val duration : Duration, val mdDistributor : MarketDataDistributor,
                              val interval: Interval, val idx : Int) {

    private var posOpenedDtGmt: Instant?  = null

    init {
        mdDistributor.addListener(interval, this::update)
        stub.tradesTopic().subscribe(this::onTrade)
    }


    private fun onTrade(trd: Trade) {
        posOpenedDtGmt = trd.dtGmt
    }

    fun update(time : Instant, ts: MarketDataDistributor): Unit {
        if (stub.position() != 0 && !ts.price(idx).interpolated &&
                Duration.between(posOpenedDtGmt,time).compareTo(duration)  > 0) {
            stub.makePositionEqualsTo(0)
        }
    }
}