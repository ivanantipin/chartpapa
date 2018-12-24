package firelib.common.misc

import java.time.Duration
import java.time.Instant

import firelib.common.*
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc

class PositionCloserByTimeOut(val stub: OrderManager, val duration : Duration) : ((TimeSeries<Ohlc>)->Unit){

    private var posOpenedDtGmt: Instant?  = null
    private val tradeSub: ChannelSubscription = stub.tradesTopic().subscribe(this::onTrade)

    fun disable(): Unit {
        tradeSub.unsubscribe()
    }

    private fun onTrade(trd: Trade) {
        posOpenedDtGmt = trd.dtGmt
    }

    fun closePositionIfTimeOut(dtGmt: Instant) {
        if (stub.position() != 0 &&  Duration.between(posOpenedDtGmt,dtGmt).compareTo(duration)  > 0) {
            stub.makePositionEqualsTo(0)
        }
    }

    override fun invoke(ts: TimeSeries<Ohlc>): Unit {
        if(!ts[0].interpolated)
            closePositionIfTimeOut(ts[0].time())
    }
}