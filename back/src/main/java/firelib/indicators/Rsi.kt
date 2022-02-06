package firelib.indicators

import firelib.core.domain.Ohlc

class Rsi(val period: Int) {
    var value = 0.0
    var prevOhlc: Ohlc? = null
    val maPos = SimpleMovingAverage(period, false)
    val maNeg = SimpleMovingAverage(period, false)

    fun addOhlc(ohlc: Ohlc): Double {
        val gain = if (prevOhlc == null) {
            ohlc.close - ohlc.open
        } else {
            ohlc.close - prevOhlc!!.close
        }
        prevOhlc = ohlc
        maPos.add(if(gain > 0) gain else 0.0)
        maNeg.add(if(gain < 0) -gain else 0.0)
        val maNeg = if (maNeg.value() < 0.0000001) 0.0000001 else maNeg.value()
        return 100 - (100 / (1 + maPos.value() / maNeg))
    }
}
