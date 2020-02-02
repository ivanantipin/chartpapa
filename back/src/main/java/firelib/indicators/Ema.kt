package firelib.indicators

import firelib.common.core.timeseries.TimeSeries
import firelib.domain.Ohlc

class Ema(
        val period: Int,
        val ts: TimeSeries<Ohlc>,
        val func: (Ohlc) -> Double = { it.close })
    : (TimeSeries<Ohlc>) -> Unit {

    fun koeffFunc(): Double {
        return 2.0 / (period + 1)
    }

    init {
        ts.preRollSubscribe(this)
    }


    private var ema: Double = 0.0

    fun value(): Double = ema

    override fun invoke(ts: TimeSeries<Ohlc>) {
        if (!ts[0].interpolated) {
            val kk = koeffFunc()
            ema = ema * (1 - kk) + func(ts[0]) * kk
        }
    }
}




