package firelib.indicators

class SimpleMovingAverage(val period: Int, val calcSko: Boolean) {

    private val closes : Array<Double> =  Array(period){0.0}
    private var currentSko = 0.0
    private var currentSma = 0.0
    private var pos = 0


    fun value ()= currentSma / period

    fun sko ()= currentSko


    fun add(cc: Double): Unit {
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