package firelib.core.timeseries

import firelib.core.domain.Ohlc

interface TimeSeries<T> {

    fun count (): Int

    operator fun get(idx: Int): T
    operator fun set(idx: Int, value : T)

    fun capacity() : Int

    fun last() : T {
        return this[0]
    }

    fun preRollSubscribe(listener : (TimeSeries<T>)->Unit)


}

fun TimeSeries<Ohlc>.ret(last : Int) : Double{
    return (this[0].close - this[last].close)/this[last].close
}

fun TimeSeries<Ohlc>.nonInterpolatedView() : TimeSeries<Ohlc>{
    val ret = TimeSeriesImpl(this.capacity(), { Ohlc() })
    this.preRollSubscribe {
        if(!it[0].interpolated){
            ret += it[0]
            ret.channel.publish(ret)
        }
    }
    return ret
}