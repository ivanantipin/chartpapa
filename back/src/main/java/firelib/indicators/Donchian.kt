package firelib.indicators

import firelib.core.domain.Ohlc
import java.util.*

class Donchian(val tailCondition : (List<Ohlc>)->Boolean) {

    val list = LinkedList<Ohlc>()

    var min: Double = Double.MAX_VALUE;
    var max: Double = Double.MIN_VALUE;

    fun add(ohlc: Ohlc) {
        list.add(ohlc)

        max = Math.max(ohlc.high, max)
        min = Math.min(ohlc.low, min)

        max = if (max.isNaN()) 0.0 else max
        min = if (min.isNaN()) 0.0 else min

        var needToRecalc = false
        while (!tailCondition(list)) {
            val last = list.pollFirst() ?: break

            if (Math.abs(last.high - max) < 0.00001 ||
                    Math.abs(last.low - min) < 0.00001) {
                needToRecalc = true
            }
        }
        if(needToRecalc){
            recalcMinMax()
        }
    }

    fun recalcMinMax() {
        if(list.isEmpty()){
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
        }else{
            min = list.minBy { it.low }!!.low
            max = list.maxBy { it.high }!!.high
        }
    }


}

fun main() {
    val donchian = Donchian({it.size <= 2})
    donchian.add(Ohlc(high = 10.0, low = 5.0))
    donchian.add(Ohlc(high = 11.0, low = 6.0))
    println("max ${donchian.max} min ${donchian.min}")
    donchian.add(Ohlc(high = 10.0, low = 7.0))
    println("max ${donchian.max} min ${donchian.min}")
}
