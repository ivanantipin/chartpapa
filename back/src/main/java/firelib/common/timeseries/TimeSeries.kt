package firelib.common.timeseries

import firelib.domain.Ohlc

interface TimeSeries<T> {

    fun count (): Int

    operator fun get(idx: Int): T
    operator fun set(idx: Int, value : T)

    fun capacity() : Int

    fun preRollSubscribe(listener : (TimeSeries<T>)->Unit)

}

fun TimeSeries<Ohlc>.diff(len : Int) : Double{
    return get(0).close - get(len - 1).close
}



