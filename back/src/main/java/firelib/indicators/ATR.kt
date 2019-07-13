package firelib.indicators

import firelib.common.misc.Quantiles
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc

class ATR(val period: Int, val ts: TimeSeries<Ohlc>) : Indicator<Double>, (TimeSeries<Ohlc>) -> Unit {

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
        return Math.max(o.range(), Math.max(o.high - ts[1].close, ts[1].close - o.high));
    }

}

class Quantiled(val period: Int, val ts : TimeSeries<Ohlc>, val func : (TimeSeries<Ohlc>)->Double){

    var value = 0.0

    init {
        val qq = Quantiles<Double>(period)

        ts.preRollSubscribe {
            val metric = func(it)
            qq.add(metric)
            value = qq.getQuantile(metric)
        }
    }

    fun value() : Double{
        return value
    }
}