package firelib.indicators


class EmaSimple(
    val period: Int,
    var ema : Double
) {
    val koeffFunc = 2.0 / (period + 1)

    fun value() = ema

    fun value(value : Double): Double = ema * (1 - koeffFunc) + value * koeffFunc

    fun onRoll(value : Double) {
        ema = ema * (1 - koeffFunc) + value * koeffFunc
    }
}


val testData = arrayOf(
22.19 to Double.NaN,
22.08 to Double.NaN,
22.17 to Double.NaN,
22.18 to Double.NaN,
22.13 to Double.NaN,
22.23 to Double.NaN,
22.43 to Double.NaN,
22.24 to Double.NaN,
22.29 to 22.22,
22.15 to 22.21)

fun main() {
    val emaSimple = EmaSimple(10, 22.27)
    testData.forEach {
        emaSimple.onRoll(it.first)
        if(!it.second.isNaN()){
            println("diff is " + (emaSimple.ema - it.second) )
        }
    }
}