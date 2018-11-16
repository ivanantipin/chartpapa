package firelib.indicators

import java.util
import java.util.Collections

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class MarketProfile  {

    private val pocChangeListeners = new ArrayBuffer[(MarketProfile)=>Unit]()

    var pocPrice: Long = 0

    val priceToVol = new util.TreeMap[Long,Long]()

    def size: Int = priceToVol.size()

    def pocVolume: Long = priceToVol(pocPrice)

    def getMap : util.NavigableMap[Long,Long] = Collections.unmodifiableNavigableMap(priceToVol)

    def add(price: Long, volume: Long): Unit = {
        priceToVol.putIfAbsent(price, 0)
        priceToVol(price) += volume
        if (price != pocPrice && priceToVol(price) > priceToVol.getOrDefault(pocPrice,-1)) {
            pocPrice = price
            firePocChange()
        }
    }


    def calcVa : (Long,Long) = {
        val allVol: Long = allVolume
        var vol = allVol - pocVolume
        var lowKey = priceToVol.lowerEntry(pocPrice)
        var upKey = priceToVol.higherEntry(pocPrice)

        while ((lowKey != null || upKey != null) && vol.toDouble/allVol > 0.2){
            if(lowKey == null){
                vol -= upKey.getValue
                upKey = priceToVol.higherEntry(pocPrice)
            }else if(upKey == null){
                vol -= lowKey.getValue
                lowKey = priceToVol.lowerEntry(pocPrice)
            }else{
                if(lowKey.getValue > upKey.getValue){
                    vol -= lowKey.getValue
                    lowKey = priceToVol.lowerEntry(pocPrice)
                }else{
                    vol -= upKey.getValue
                    upKey = priceToVol.higherEntry(pocPrice)
                }
            }
        }
        val l: Long = if (lowKey == null) priceToVol.firstKey() else lowKey.getKey
        val h: Long = if (upKey == null) priceToVol.lastKey() else upKey.getKey
        (l ,h)
    }

    def listenPocChanges(ls : (MarketProfile)=>Unit) : Unit = pocChangeListeners += ls

    private def firePocChange(): Unit = pocChangeListeners.foreach(_.apply(this))

    def reduceBy(price: Long, volume: Long) : Unit = {
        var nvol = priceToVol(price) - volume
        priceToVol(price) = nvol
        if (price == pocPrice) {
            recalcPoc()
        }
        assert(nvol >= 0, "must never be negative")
    }

    private def recalcPoc() = {
        pocPrice = priceToVol.maxBy(_._2)._1
        firePocChange()
    }

    def allVolume: Long = priceToVol.values().sum

    def volumeAtPrice(price: Long): Long = priceToVol.getOrDefault(price, 0)
}
