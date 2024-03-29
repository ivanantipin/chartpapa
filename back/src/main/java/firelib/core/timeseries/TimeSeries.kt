package firelib.core.timeseries

import firelib.core.domain.Ohlc
import firelib.core.domain.ret
import firelib.core.mddistributor.forceMerge
import firelib.core.mddistributor.mergeOhlc
import firelib.core.misc.atNy
import java.time.Instant
import java.time.LocalTime

interface TimeSeries<T> {
    fun count (): Int
    operator fun get(idx: Int): T
    operator fun set(idx: Int, value : T)
    fun capacity() : Int
    fun preRollSubscribe(listener : (TimeSeries<T>)->Unit)
    fun last() : T {
        return this[0]
    }
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

class ConditionalTimeSeries(val condition : (Ohlc)->Boolean,
                            val rollCondition : (Ohlc)->Boolean,
                            val delegate : TimeSeries<Ohlc>

) : TimeSeries<Ohlc>{

    init {
        delegate.preRollSubscribe {
            if(condition(it[0])){
                series[0].forceMerge(it[0])
                if(rollCondition(series[0])){
                    series.channel.publish(series)
                    series += series[0].copy(interpolated = true)
                }
            }
        }
    }

    val series = TimeSeriesImpl<Ohlc>(100, { Ohlc() })

    override fun count(): Int {
        return series.count()
    }

    override fun get(idx: Int): Ohlc {
        return series.get(idx)
    }

    override fun set(idx: Int, value: Ohlc) {
        series.set(idx, value)
    }

    override fun capacity(): Int {
        return series.capacity()
    }

    override fun preRollSubscribe(listener: (TimeSeries<Ohlc>) -> Unit) {
        series.preRollSubscribe(listener)
    }
}

fun TimeSeries<Ohlc>.indexOfTime(time : Instant ) : Int {
    val cnt = this.count()
    for(i in 0 until cnt){
        if(this[i].endTime == time){
            return i
        }
    }
    return -1
}

fun TimeSeries<Ohlc>.returns(cnt : Int) : DoubleArray {
    require(cnt < count())
    return DoubleArray(cnt, {this[it].ret()})
}

fun TimeSeries<Ohlc>.toSequence(start : Int) : Sequence<Ohlc> {
    require(start < count())
    return (0 .. start).asSequence().map {idx->
        this[start  - idx]
    }
}



fun makeUsTimeseries(it: TimeSeries<Ohlc>): ConditionalTimeSeries {
    val startTime = LocalTime.of(9, 30)
    val endTime = LocalTime.of(16, 0)
    return ConditionalTimeSeries({ oh ->
        val nyTime = oh.endTime.atNy().toLocalTime()!!
        nyTime >= startTime && nyTime <= endTime
    }, { oh ->
        val nyTime = oh.endTime.atNy().toLocalTime()!!
        nyTime == endTime
    }, it)
}
