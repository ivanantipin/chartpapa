package firelib.indicators

import org.apache.commons.collections.MultiMap
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MarketProfile {

    private val pocChangeListeners = ArrayList<(MarketProfile) -> Unit>()

    var pocPrice: Long = 0

    val priceToVol = TreeMap<Long, Long>()

    val hist = HashMap<Long,ArrayList<Long>>()

    fun size(): Int {
        return priceToVol.size
    }

    fun pocVolume(): Long {
        return priceToVol[pocPrice]!!
    }

    var allVolume = 0L

    fun add(price: Long, volume: Long) {
        priceToVol.compute(price) { k, v -> if (v == null) volume else v + volume }

        hist.computeIfAbsent(price,{ArrayList<Long>()}).add(volume)

        allVolume += volume

        if (price != pocPrice && priceToVol[price]!! > priceToVol.getOrDefault(pocPrice, -1)) {
            pocPrice = price
            firePocChange()
        }
    }


    fun calcVaRange(): Pair<Long, Long> {
        val allVol: Long = allVolume()
        var vol = allVol - pocVolume()
        var lowKey = priceToVol.lowerEntry(pocPrice)
        var upKey = priceToVol.higherEntry(pocPrice)

        while ((lowKey != null || upKey != null) && vol.toDouble() / allVol > 0.3) {
            if (lowKey == null) {
                vol -= upKey.value
                upKey = priceToVol.higherEntry(upKey.key)
            } else if (upKey == null) {
                vol -= lowKey.value
                lowKey = priceToVol.lowerEntry(lowKey.key)
            } else {
                if (lowKey.value > upKey.value) {
                    vol -= lowKey.value
                    lowKey = priceToVol.lowerEntry(lowKey.key)
                } else {
                    vol -= upKey.value
                    upKey = priceToVol.higherEntry(upKey.key)
                }
            }
        }
        val l: Long = if (lowKey == null) priceToVol.firstKey() else lowKey.key
        val h: Long = if (upKey == null) priceToVol.lastKey() else upKey.key
        return Pair(l, h)
    }

    fun listenPocChanges(ls: (MarketProfile) -> Unit) {
        pocChangeListeners += ls
    }

    fun printStr() : String{
        return "${priceToVol.firstEntry().key} | ${priceToVol.values.joinToString (separator = ",")} | ${priceToVol.lastEntry().key}"
    }


    private fun firePocChange(): Unit = pocChangeListeners.forEach({ it(this) })


    fun reduceBy(price: Long, volume: Long): Unit {
        allVolume -= volume
        priceToVol.compute(price) { k, v ->
            require(v!! >= volume)
            v - volume
        }
        if (price == pocPrice) {
            recalcPoc()
        }
    }

    private fun recalcPoc() {
        pocPrice = priceToVol.maxBy { it.value }!!.key
        firePocChange()
    }

    fun allVolume(): Long = allVolume

    operator fun get(price : Long) = priceToVol.getOrDefault(price, 0)

}

fun main() {
    val profile = MarketProfile()

    for(i in 1..10){
        profile.add(i.toLong(), i.toLong())
    }

    println(profile.calcVaRange())
    println(profile.pocPrice)

    profile.reduceBy(10L, 10L)
    profile.reduceBy(9L, 9L)
    profile.reduceBy(8L, 8L)

    println(profile.calcVaRange())
    println(profile.pocPrice)



}