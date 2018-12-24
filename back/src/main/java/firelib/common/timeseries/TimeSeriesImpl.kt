package firelib.common.timeseries

import firelib.common.misc.NonDurableChannel
import firelib.common.misc.SubChannel

class TimeSeriesImpl<T>(val length: Int, val func: (Int) -> T) : TimeSeries<T> {

    override fun subscribe(listener: (TimeSeries<T>) -> Unit) {
        channel.subscribe(listener)
    }

    val data = RingBuffer<T>(length, func)

    val channel = NonDurableChannel<TimeSeries<T>>()

    override fun count(): Int {
        return data.count
    }

    override operator fun get(idx: Int): T {
        return data[idx]
    }

    override fun set(idx: Int, value: T) {
        data[idx] = value
    }

    operator fun plusAssign(t: T) {
        channel.publish(this)
        data.add(t)
    }
}