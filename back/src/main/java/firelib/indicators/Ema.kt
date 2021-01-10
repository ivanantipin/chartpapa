package firelib.indicators

import firelib.core.domain.Ohlc
import firelib.core.timeseries.TimeSeries

class Ema(
    val period: Int,
    val ts: TimeSeries<Ohlc>,
    val func: (Ohlc) -> Double = { it.close }
) : (TimeSeries<Ohlc>) -> Unit {

    val koeffFunc = 2.0 / (period + 1)

    init {
        ts.preRollSubscribe(this)
    }


    private var ema: Double = 0.0

    fun value(): Double = ema * (1 - koeffFunc) + func(ts[0]) * koeffFunc

    override fun invoke(ts: TimeSeries<Ohlc>) {
        if (!ts[0].interpolated) {
            ema = ema * (1 - koeffFunc) + func(ts[0]) * koeffFunc
        }
    }
}