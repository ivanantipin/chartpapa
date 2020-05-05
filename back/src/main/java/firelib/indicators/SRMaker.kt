package firelib.indicators

import firelib.core.domain.Ohlc
import java.time.Instant
import java.util.*

class SRMaker (val tolerance : Double){
    val zigZag = ZigZag(0.01)

    val extremes = mutableListOf<Pair<Instant,Double>>()

    var currentLevels = emptyList<Double>()

    init{
        zigZag.addListener {
            extremes += Pair(it.data.last().time(), it.extreme)
            currentLevels = mergeExtremes()
        }
    }

    fun mergeExtremes() : List<Double>{
        if(extremes.isEmpty()){
            return emptyList()
        }
        val sorted = LinkedList(extremes.map { it.second }.sorted())
        val ret = mutableListOf<List<Double>>()


        var num = mutableListOf(sorted.poll()!!)


        while (sorted.isNotEmpty()){
            val poll = sorted.poll()!!
            if(poll - num.last() > tolerance){
                ret += num
                num = mutableListOf(poll)
            }else{
                num.add(poll)
            }
        }
        ret += num
        return ret.map { it.average() }
    }

    fun addOhlc(ohlc: Ohlc){
        zigZag.addOhlc(ohlc)
    }
}