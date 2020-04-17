package firelib.model.prod

import firelib.core.*
import firelib.core.domain.Interval
import firelib.core.domain.downShadow
import firelib.core.domain.range
import firelib.core.domain.upShadow
import firelib.core.misc.Quantiles
import firelib.indicators.MarketProfile
import firelib.indicators.SimpleMovingAverage
import java.lang.Exception


fun Model.enableBarQuantFactor(){
    val daytss = enableSeries(Interval.Day)
    enableFactor("barQuant") {
        daytss[it][0].upShadow()/daytss[it][0].range()
    }

}

fun Model.enableBarQuantLowFactor() : (idx : Int)->Double{
    val daytss = enableSeries(Interval.Day)
    val ret = {idx : Int->
        daytss[idx][0].downShadow() / daytss[idx][0].range()
    }
    enableFactor("barQuantLow") {
        ret(it)
    }
    return ret
}

fun Model.avgBarQuantLow(period : Int){
    val daytss = enableSeries(Interval.Day)

    val mas = instruments().map { SimpleMovingAverage(period,false) }

    fun fac(idx : Int) : Double{
        return daytss[idx][0].downShadow() / daytss[idx][0].range()
    }

    enableFactor("avgBarQuantLow${period}") {
        (mas[it].value()*period + fac(it))/(period + 1.0)
    }

    daytss.forEachIndexed{idx,ts->
        ts.preRollSubscribe {
            if(!it[0].interpolated){
                mas[idx].add(fac(idx))
            }
        }
    }
}



fun Model.enablePocFactor(profiles : List<MarketProfile>, increments : DoubleArray){
    val daytss = enableSeries(Interval.Day)
    val quantiles = quantiles(300)

    fun calcFactor(idx : Int) : Double {
        try {
            return daytss[idx][0].close - profiles[idx].pocPrice*increments[idx]
        }catch (e : Exception){
            e.printStackTrace()
            return 0.0
        }

    }
    enableFactor("poc") {
        val ret = quantiles[it].getQuantile(calcFactor(it))
        if(ret.isFinite()) ret else -1.0
    }

    daytss.forEachIndexed { idx, it ->
        it.preRollSubscribe {
            if(!it[0].interpolated){
                quantiles[idx].add(calcFactor(idx))
            }
        }
    }
}




fun Model.enableVolumeFactor(){
    val daytss = enableSeries(Interval.Day)
    val volumeQuantiles = quantiles(300)

    enableFactor("volume") {
        val ret = volumeQuantiles[it].getQuantile(daytss[it].last().volume.toDouble())
        if (ret.isNaN()) -1.0 else ret
    }

    daytss.forEachIndexed { idx, it ->
        it.preRollSubscribe {
            if(!it[0].interpolated){
                volumeQuantiles[idx].add(it[0].volume.toDouble())
            }
        }
    }

}

fun Model.enableMaDiffFactor(period : Int = 30){
    val daytss = enableSeries(Interval.Day)
    val quantiles = instruments().map {
        Quantiles<Double>(300);
    }

    val mas = instruments().map { SimpleMovingAverage(period, false) }

    enableFactor("madiff${period}") {
        val ret = quantiles[it].getQuantile(daytss[it][0].close - mas[it].value())
        if (ret.isNaN()) -1.0 else ret
    }

    daytss.forEachIndexed { idx, it ->
        it.preRollSubscribe {
            if(!it[0].interpolated){
                quantiles[idx].add(it[0].close - mas[idx].value())
            }
        }
    }

}


