package firelib.indicators

class SimpleMovingAverage(val period: Int, val calcSko: Boolean) {


    private val closes = Array.fill[Double](period)(0)

    private var currentSko = 0.0
    private var currentSma = 0.0
    private var pos = 0


    def value = currentSma / period

    def sko = currentSko


    def add(cc: Double): Unit = {
        if (cc.isNaN) {
            return
        }
        currentSma -= closes(pos)
        currentSma += cc
        closes(pos) = cc
        pos += 1
        if (pos == closes.length) {
            pos = 0;
        }

        if (calcSko) {
            var sig = 0.0
            var i = 0
            while (i < period){
                val cl = closes(i)
                sig += (cl - value) * (cl - value)
                i+=1
            }
            sig /= period;
            currentSko = math.pow(sig, 0.5);
        }
    }

}
