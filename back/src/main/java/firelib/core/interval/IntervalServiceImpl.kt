package firelib.core.interval

import firelib.core.domain.Interval
import java.time.Instant


class IntervalServiceImpl : IntervalService {

    var interval2listeners = listOf<Pair<Interval, List<(Instant) -> Unit>>>()

    override fun addListener(interval: Interval, action: (Instant) -> Unit) {
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
            if (dt.toEpochMilli() % i.first.durationMs == 0L) {
                i.second.forEach {
                    try {
                        it(dt)
                    }catch (e : Exception){
                        println("error in listener ${e.message}")
                        e.printStackTrace()
                    }

                }
                ret += i.first
            }
        }
        return ret
    }
}