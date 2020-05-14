package firelib.core.timeseries

import firelib.core.misc.NonDurableChannel

class TimeSeriesImpl<T>(val capacity: Int, val func: (Int) -> T) : TimeSeries<T> {

    val channel = NonDurableChannel<TimeSeries<T>>()

    override fun capacity(): Int {
        return data.capacity
    }

    override fun preRollSubscribe(listener: (TimeSeries<T>) -> Unit) {
        channel.subscribe(listener)
    }


    var data = RingBuffer<T>(capacity, func)

    override fun count(): Int {
        return data.count
    }

    fun adjustCapacity(ncapacity : Int){
      data = data.copyWithAdjustedCapacity(ncapacity)
    }

    override operator fun get(idx: Int): T {
        return data[idx]
    }

    override fun set(idx: Int, value: T) {
        data[idx] = value
    }

    operator fun plusAssign(t: T) {
        data.add(t)
    }
}