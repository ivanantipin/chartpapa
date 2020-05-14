package firelib.indicators

import firelib.core.domain.Ohlc
import java.time.Instant
import java.util.*


data class SR(val initial : Instant, val activeDate : Instant, val level : Double)

class SRMaker (val tolerance : Double, val numberOfExtremes : Int = 100, val numberOfHits : Int = 2, val zigZagMove : Double = 0.02){
    val zigZag = ZigZag(zigZagMove)

    val extremes = LinkedList<Pair<Instant,Double>>()

    var currentLevels = emptyList<SR>()


    var evictedSr : ((List<SR>) -> Unit)? = null

    fun setEvictedListener(listener : (List<SR>)->Unit){
        evictedSr = listener
    }

    init{
        zigZag.addListener {
            extremes += Pair(it.data.last().time(), it.extreme)
            if(extremes.size > numberOfExtremes ){
                extremes.poll()
            }

            val tmp = mergeExtremes()

            if(evictedSr != null){
                val evicted = currentLevels.filter { cl -> !tmp.any { it.initial == cl.initial && it.activeDate == cl.activeDate } }
                evictedSr!!(evicted)
            }
            currentLevels = tmp
        }
    }

    fun mergeExtremes() : List<SR> {
        if(extremes.isEmpty()){
            return emptyList()
        }
        val sorted = LinkedList(extremes.sortedBy { it.second })
        val ret = mutableListOf<List<Pair<Instant,Double>>>()


        var num = mutableListOf(sorted.poll()!!)

        while (sorted.isNotEmpty()){
            val poll = sorted.poll()!!
            val diff = (poll.second - num.last().second)*2/(poll.second + num.last().second)
            if(diff > tolerance){
                ret += num
                num = mutableListOf(poll)
            }else{
                num.add(poll)
            }
        }
        ret += num

        return ret.filter { it.size >= numberOfHits}.map { it.sortedBy { it.first } }.map {
            SR(it[0].first,it[numberOfHits - 1].first, it.map { it.second }.average())
        }
    }

    fun addOhlc(ohlc: Ohlc){
        zigZag.addOhlc(ohlc)
    }
}