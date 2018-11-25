package firelib.indicators

import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc

class ATR(val period: Int, val ts: TimeSeries<Ohlc>) : Indicator<Double>, (TimeSeries<Ohlc>) -> Unit {

    init {
        ts.onNewBar().subscribe(this)
    }

    val avg = SimpleMovingAverage(period, false)

    override fun value(): Double {
        return avg.value()
    }

    override fun invoke(v1: TimeSeries<Ohlc>): Unit {
        avg.add(lastRange(v1))
    }

    private fun lastRange(ts: TimeSeries<Ohlc>): Double {
        val o = ts[0]
        if (o.interpolated) {
            return Double.NaN;
        }
        if (ts.count() == 1)
            return o.H - o.L;
        return Math.max(o.H - o.L, Math.max(o.H - ts[-1].C, ts[-1].C - o.H));
    }

}