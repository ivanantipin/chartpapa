package firelib.indicators

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MarketProfile {

    private val pocChangeListeners = ArrayList<(MarketProfile) -> Unit>()

    var pocPrice: Int = 0

    val priceToVol = FastArray()


    fun pocVolume(): Long {
        return priceToVol[pocPrice]
    }

    var allVolume = 0L

    fun add(price: Int, volume: Long) {
        priceToVol.inc(price, volume)
        allVolume += volume
        if (price != pocPrice && priceToVol[price] > priceToVol[pocPrice]) {
            pocPrice = price
            firePocChange()
        }
    }


    fun calcVaRange(): Pair<Int, Int> {
        val allVol: Long = allVolume()
        var vol = allVol - pocVolume()
        var lowKey = pocPrice - 1
        var upKey = pocPrice + 1

        val data = priceToVol.data
        while ((lowKey >= 0 || upKey < data.size) && vol.toDouble() / allVol > 0.3) {
            if (lowKey < 0) {
                vol -=  data[upKey]
                upKey += 1
            } else if (upKey >= data.size) {
                vol -= data[lowKey]
                lowKey -= 1
            } else {
                if (data[lowKey] > data[upKey]) {
                    vol -= data[lowKey]
                    lowKey -= 1
                } else {
                    vol -=  data[upKey]
                    upKey += 1
                }
            }
        }
        val l: Int = if (lowKey <0 ) 0 else lowKey
        val h: Int = if (upKey >= data.size ) data.size - 1 else upKey
        return Pair(l, h)
    }

    fun listenPocChanges(ls: (MarketProfile) -> Unit) {
        pocChangeListeners += ls
    }

    private fun firePocChange(): Unit = pocChangeListeners.forEach({ it(this) })


    fun reduceBy(price: Int, volume: Long) {
        allVolume -= volume
        priceToVol.dec(price, volume)
        if (price == pocPrice) {
            recalcPoc()
        }
    }

    private fun recalcPoc() {
        var maxIdx = 0
        priceToVol.data.forEachIndexed({idx, vv->
            if(vv > priceToVol.data[maxIdx]){
                maxIdx = idx
            }
        })
        pocPrice = maxIdx
        firePocChange()
    }

    fun allVolume(): Long = allVolume

    operator fun get(price : Int) = priceToVol[price]

}


// up levels means that volume increase and vise versa
fun MarketProfile.defineLevels(window : Int, upLevels : Boolean, numberOfLevels : Int = 2) : List<Pair<Int, Long>>{

    val priorityQueue = if(upLevels){
        PriorityQueue<Pair<Int,Long>>({p0,p1-> p0.second.compareTo(p1.second)})
    }else{
        PriorityQueue<Pair<Int,Long>>({p0,p1-> p1.second.compareTo(p0.second)})
    }


    val dt = this.priceToVol.data

    val halfWindow = window/2

    for(i in this.priceToVol.start + halfWindow .. this.priceToVol.end - halfWindow + 1){

        val v0 = dt.sum(i - halfWindow, i)
        val v1 = dt.sum(i, i + halfWindow)

        priorityQueue += Pair(i, v1 - v0)

        if(priorityQueue.size > numberOfLevels){
            priorityQueue.poll()
        }
    }

    return priorityQueue.sortedBy { it.second }
}

fun LongArray.sum(start : Int, end : Int) : Long{
    var v0 = 0L
    for ( x in start until end){
        v0 += this[x]
    }
    return v0
}

class FastArray{
    var data : LongArray = LongArray(1,{0})

    var start : Int = Int.MAX_VALUE
    var end : Int = Int.MIN_VALUE

    fun inc(idx : Int, value : Long){
        resizeForIdx(idx)
        if(idx >= data.size){
            println()
            resizeForIdx(idx)
        }
        data[idx] += value
        if(idx < start){
            start = idx
        }
        if(idx > end){
            end = idx
        }

    }

    fun dec(idx : Int, value : Long){
        data[idx] -= value
        if(idx == start && data[start] == 0L){
            while (start < data.size && data[start] == 0L){
                start++
            }
        }
        if(idx == end && data[end] == 0L){
            while (end >= 0 && data[end] == 0L){
                end--
            }
        }

    }

    private fun resizeForIdx(idx: Int) {
        if (idx >= data.size) {
            data = LongArray(Integer.max(data.size * 2, idx * 2), { idx ->
                if (idx < data.size) {
                    data[idx]
                } else {
                    0L
                }
            })
        }
    }

    operator fun get(key : Int) : Long{
        resizeForIdx(key)
        return data[key]
    }

}


fun main() {
    val profile = MarketProfile()

    for(i in 1..10){
        profile.add(i, i.toLong())
    }

    require(Pair(5,11) == profile.calcVaRange(), {"${profile.calcVaRange()}"})
    require(10 == profile.pocPrice)

    profile.reduceBy(10, 10L)
    profile.reduceBy(9, 9L)
    profile.reduceBy(8, 8L)


    require(Pair(3,8) == profile.calcVaRange(), {"${profile.calcVaRange()}"})
    require(7 == profile.pocPrice)


    val profileForLevels = MarketProfile()

    profileForLevels.add(1,1)
    profileForLevels.add(2,1)
    profileForLevels.add(3,3)
    profileForLevels.add(4,3)
    profileForLevels.add(5,3)
    profileForLevels.add(6,4)
    profileForLevels.add(7,4)
    profileForLevels.add(8,6)

    val levels = profileForLevels.defineLevels(4, true)

    require(levels[1] == Pair(3,4L), {"${levels[1]}"})
    require(levels[0] == Pair(7,3L), {"${levels[0]}"})


}