package firelib.indicators

import firelib.common.core.timeseries.TimeSeries
import firelib.domain.Ohlc
import kotlinx.coroutines.runBlocking


class Ma(val period: Int, val ts: TimeSeries<Ohlc>, val calcSko: Boolean = false)
    : (TimeSeries<Ohlc>) -> Unit {

    init {
        ts.preRollSubscribe({ runBlocking { invoke(it)  }})
    }

    val maa = SimpleMovingAverage(period, calcSko)

    fun value(): Double = maa.value()

    fun sko(): Double = maa.sko()

    override fun invoke(ts: TimeSeries<Ohlc>) {
        if (!ts[0].interpolated) {
            maa.add(ts[0].close)
        }
    }
}