package firelib.indicators

import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc


class Ma(val period: Int, val ts: TimeSeries<Ohlc>, val calcSko: Boolean = false)
    : Indicator<Double>, (TimeSeries<Ohlc>) -> Unit {

    init {
        ts.onNewBar().subscribe(this)
    }

    val maa = SimpleMovingAverage(period, calcSko)

    override fun value(): Double = maa.value()

    fun sko(): Double = maa.sko()

    override fun invoke(ts: TimeSeries<Ohlc>): Unit {
        if (!ts[0].interpolated) {
            maa.add(ts[0].close)
        }
    }
}