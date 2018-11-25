package firelib.common.timeseries

import firelib.common.misc.SubChannel
import firelib.domain.Ohlc

interface TimeSeries<T> {

    fun count (): Int

    operator fun get(idx: Int): T
    operator fun set(idx: Int, value : T)

    fun onNewBar() : SubChannel<TimeSeries<T>>

}

fun TimeSeries<Ohlc>.diff(len : Int) : Double{
    return get(0).C - get(len - 1).C
}



