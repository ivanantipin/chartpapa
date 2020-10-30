package firelib.indicators

import firelib.core.domain.Ohlc
import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.commons.math3.ml.clustering.DBSCANClusterer
import java.time.Instant
import java.util.*

inline class Cl(val point : Pair<Instant,Double>) : Clusterable {
    override fun getPoint(): DoubleArray {
        return doubleArrayOf(point.second)
    }
}

data class SR(val initial : Instant, val activeDate : Instant, val level : Double)

class SRMaker(val numberOfExtremes: Int = 100, val numberOfHits: Int = 2, val zigZagMove: Double = 0.02){
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

        val avg = extremes.map { it.second }.average()

        val data = extremes.map { Cl(it) }

        val clasterer = DBSCANClusterer<Cl>(avg/300, numberOfHits)

        val cluster = clasterer.cluster(data)

        return cluster.map {
            val first = it.points.minByOrNull { it.point.first }!!.point.first
            val last = it.points.maxByOrNull { it.point.first }!!.point.first
            SR(first,last, it.points.map { it.point.second }.average())
        }
    }

    fun addOhlc(ohlc: Ohlc){
        zigZag.addOhlc(ohlc)
    }
}