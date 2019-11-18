package firelib.indicators

import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import firelib.domain.range

class ATR(val period: Int, val ts: TimeSeries<Ohlc>) {

    init {
        ts.preRollSubscribe({avg.add(lastRange(it))})
    }

    val avg = SimpleMovingAverage(period, false)

    fun value(): Double {
        return avg.value()
    }

    private fun lastRange(ts: TimeSeries<Ohlc>): Double {
        val o = ts[0]
        if (o.interpolated) {
            return Double.NaN;
        }
        if (ts.count() == 1)
            return o.high - o.low;
        return Math.max(o.range(), Math.max(o.high - ts[1].close, ts[1].close - o.high));
    }

}
