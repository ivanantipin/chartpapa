package firelib.indicators

import java.lang.RuntimeException
import kotlin.math.absoluteValue
import kotlin.random.Random

class SimpleMovingAverage(val period: Int, val calcSko: Boolean) {

    private val closes : Array<Double> =  Array(period){0.0}
    private var currentSko = 0.0
    private var currentSma = 0.0
    private var pos = 0


    fun value ()= currentSma / period

    fun sko ()= currentSko


    fun add(cc: Double) {
        if (cc.isNaN()) {
            return
        }
        currentSma += (cc - closes[pos])
        closes[pos] = cc
        pos = (pos + 1)%closes.size

        if (calcSko) {
            val value = value()
            currentSko = Math.pow(closes.sumByDouble {
                it - value
            }/period, 0.5);
        }
    }
}

fun main() {
    val sa = SimpleMovingAverage(10,false)
    val lst = mutableListOf<Double>()
    val random = Random(System.currentTimeMillis())
    repeat(100) {
        val rnd = random.nextDouble()
        sa.add(rnd)
        lst.add(rnd)

        if(lst.size > 10){
            lst.removeAt(0)
        }

        if(lst.size == 10){
            if( (sa.value() - lst.sum()/lst.size).absoluteValue > 0.00001){
                throw RuntimeException("test failed ${sa.value()} <> ${lst.sum()}")
            }

        }
    }

}