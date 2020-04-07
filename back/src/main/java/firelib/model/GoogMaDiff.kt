package firelib.model

import firelib.indicators.SimpleMovingAverage
import java.time.Instant
import java.util.*

class GoogMaDiff(val ts : List<Pair<Instant, Double>>, period : Int){

    val trendma = SimpleMovingAverage(period, true)

    val queue = LinkedList(ts)
    var latest : Pair<Instant,Double>? = null


    fun getDiff(time : Instant) : Double{
        var peek : Pair<Instant,Double>? = queue.peek()
        while (peek != null && peek.first < time){
            if(latest != null){
                trendma.add(latest!!.second)
            }
            latest = queue.poll()
            peek = queue.peek()
        }
        if(latest == null) return 0.0;
        return latest!!.second - trendma.value();

    }
}