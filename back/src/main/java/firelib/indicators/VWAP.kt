package firelib.indicators

import firelib.core.domain.Ohlc

class VWAP (val period : Int){
    val ma0 = SimpleMovingAverage(period, false)
    val ma1 = SimpleMovingAverage(period, false)

    fun addOhlc(oh : Ohlc){
        ma0.add(oh.close*oh.volume)
        ma1.add(oh.volume.toDouble())
    }

    fun value() : Double{
        return ma0.value()/ma1.value()
    }
}