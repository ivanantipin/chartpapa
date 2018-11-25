package firelib.indicators

import java.util.*

class MarketProfile  {

    private val pocChangeListeners = ArrayList<(MarketProfile)->Unit>()

    var pocPrice: Long = 0

    val priceToVol = TreeMap<Long,Long>()

    fun size(): Int {return priceToVol.size}

    fun pocVolume(): Long { return priceToVol[pocPrice]!!}

    fun getMap (): NavigableMap<Long, Long> = Collections.unmodifiableNavigableMap(priceToVol)

    fun add(price: Long, volume: Long): Unit {
        priceToVol.putIfAbsent(price, 0)
        priceToVol[price] = priceToVol[price]!! + volume
        if (price != pocPrice && priceToVol[price]!! > priceToVol.getOrDefault(pocPrice,-1)) {
            pocPrice = price
            firePocChange()
        }
    }


    fun calcVa (): Pair<Long,Long> {
        val allVol: Long = allVolume()
        var vol = allVol - pocVolume()
        var lowKey = priceToVol.lowerEntry(pocPrice)
        var upKey = priceToVol.higherEntry(pocPrice)

        while ((lowKey != null || upKey != null) && vol.toDouble()/allVol > 0.2){
            if(lowKey == null){
                vol -= upKey.value
                upKey = priceToVol.higherEntry(pocPrice)
            }else if(upKey == null){
                vol -= lowKey.value
                lowKey = priceToVol.lowerEntry(pocPrice)
            }else{
                if(lowKey.value > upKey.value){
                    vol -= lowKey.value
                    lowKey = priceToVol.lowerEntry(pocPrice)
                }else{
                    vol -= upKey.value
                    upKey = priceToVol.higherEntry(pocPrice)
                }
            }
        }
        val l: Long = if (lowKey == null) priceToVol.firstKey() else lowKey.key
        val h: Long = if (upKey == null) priceToVol.lastKey() else upKey.key
        return Pair(l ,h)
    }

    fun listenPocChanges(ls : (MarketProfile)->Unit) : Unit {pocChangeListeners += ls}

    private fun firePocChange(): Unit = pocChangeListeners.forEach({it(this)})

    fun reduceBy(price: Long, volume: Long) : Unit {
        var nvol = priceToVol[price]!! - volume
        priceToVol[price] = nvol
        if (price == pocPrice) {
            recalcPoc()
        }
        assert(nvol >= 0, {"must never be negative"})
    }

    private fun recalcPoc() {
        pocPrice = priceToVol.maxBy{it.value}!!.key
        firePocChange()
    }

    fun allVolume(): Long = priceToVol.values.sum()

    fun volumeAtPrice(price: Long): Long = priceToVol.getOrDefault(price, 0)
}