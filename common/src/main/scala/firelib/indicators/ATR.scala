package firelib.indicators

import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc

class ATR(period: Int, ts: TimeSeries[Ohlc]) extends Indicator[Double] with (TimeSeries[Ohlc] => Unit) {

    override def value: Double = avg.value

    val avg = new SimpleMovingAverage(period, false)

    ts.onNewBar.subscribe(this)

    override def apply(v1: TimeSeries[Ohlc]): Unit = {
        avg.add(lastRange(v1))
    }

    private def lastRange(ts: TimeSeries[Ohlc]): Double = {
        val o = ts(0)
        if (o.interpolated) {
            return Double.NaN;
        }
        if (ts.count == 1)
            return o.H - o.L;
        return math.max(o.H - o.L, math.max(o.H - ts(-1).C, ts(-1).C - o.H));
    }

}
