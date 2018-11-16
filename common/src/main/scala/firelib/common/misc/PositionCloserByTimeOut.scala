package firelib.common.misc

import java.time.{Duration, Instant}

import firelib.common._
import firelib.common.ordermanager.OrderManager
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc

class PositionCloserByTimeOut(val stub: OrderManager, val duration : Duration) extends (TimeSeries[Ohlc]=>Unit){

    private var posOpenedDtGmt: Instant  = _
    private val tradeSub: ChannelSubscription = stub.tradesTopic.subscribe(onTrade)

    def disable(): Unit ={
        tradeSub.unsubscribe()
    }

    private def onTrade(trd: Trade) = {
        posOpenedDtGmt = trd.dtGmt
    }

    def closePositionIfTimeOut(dtGmt: Instant) = {
        if (stub.position != 0 &&  Duration.between(posOpenedDtGmt,dtGmt).compareTo(duration)  > 0) {
            stub.managePosTo(0)
        }
    }

    override def apply(ts: TimeSeries[Ohlc]): Unit = {
        if(!ts(0).interpolated)
            closePositionIfTimeOut(ts(0).time)
    }
}
