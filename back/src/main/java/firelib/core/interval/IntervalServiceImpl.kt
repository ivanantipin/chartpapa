package firelib.core.interval

import firelib.core.domain.Interval
import java.time.Instant
import java.util.concurrent.Callable


class IntervalServiceImpl : IntervalService {

    var interval2listeners = listOf<Pair<Interval, List<IServiceAction>>>()

    override fun addListener(interval: Interval, action: IServiceAction) {
        if(!interval2listeners.any { it.first == interval }){
            interval2listeners += Pair(interval, emptyList())
        }
        interval2listeners = interval2listeners.map {
            if (it.first == interval) {
                Pair(it.first, it.second + action)
            } else {
                it
            }
        }
    }

    fun onStep(dt: Instant) : List<Interval>{
        val ret = ArrayList<Interval>(0)
        for (i in interval2listeners) {
            val interval = i.first
            if ((dt.toEpochMilli() - interval.offset)  % interval.durationMs == 0L) {
                i.second.
                forEach {
                    try {
                        it(dt)
                    }catch (e : Exception){
                        println("error in listener ${e.message}")
                        e.printStackTrace()
                    }

                }
                ret += interval
            }
        }
        return ret
    }
}