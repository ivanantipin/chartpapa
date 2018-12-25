package firelib.indicators

import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc

class ATR(val period: Int, val ts: TimeSeries<Ohlc>) : Indicator<Double>, (TimeSeries<Ohlc>) -> Unit {
    override fun calculate() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init {
        ts.preRollSubscribe(this)
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
            return o.high - o.low;
        return Math.max(o.high - o.low, Math.max(o.high - ts[-1].close, ts[-1].close - o.high));
    }

}