package firelib.model.prod

import firelib.core.*
import firelib.core.domain.*
import firelib.core.misc.Quantiles
import firelib.core.misc.atMoscow
import firelib.core.timeseries.ret
import firelib.indicators.MarketProfile
import firelib.indicators.SimpleMovingAverage
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min


fun Model.factorWeekday() {
    enableDiscreteFactor("weekday_int") {
        currentTime().atMoscow().toLocalDate().dayOfWeek.value
    }
}

fun Model.factorHour() {
    enableFactor("hour_int") {
        currentTime().atMoscow().hour.toDouble()
    }
}

fun Model.factorBarQuant() {
    val daytss = enableSeries(Interval.Day)
    enableFactor("barQuant") {
        daytss[it][0].upShadow() / daytss[it][0].range()
    }
}

fun Model.enableCorrMatrix(): (i: Int, j: Int) -> Double {
    var corrMatrix: RealMatrix? = null

    val weekTs = enableSeries(Interval.Week, 300)

    weekTs[0].preRollSubscribe {
        if (it.count() > 20) {
            val cc = min(it.count() - 1, 70)
            val corr = PearsonsCorrelation(Array(cc, { i -> DoubleArray(weekTs.size, { j -> weekTs[j][i].ret() }) }))
            corrMatrix = corr.correlationMatrix
        }
    }

    return { i, j ->
        if (corrMatrix != null) corrMatrix!!.getEntry(i, j) else Double.NaN
    }

}

fun Model.factorRank(daysBack: Int): (idx: Int) -> Int {
    val sers = enableSeries(Interval.Day, daysBack + 1)
    val nonInterp = enableSeries(Interval.Day, daysBack + 1, false)

    val ret = { tickerIdx: Int ->
        val idxToRet = sers.mapIndexed { idx, _ ->
            var ret = (sers[idx][0].close - nonInterp[idx][daysBack].close) / nonInterp[idx][daysBack].close
            Pair(idx, ret)
        }
        val sortedBy = idxToRet.sortedBy { -it.second }
        sortedBy.indexOfFirst { it.first == tickerIdx }
    }

    enableDiscreteFactor("rank${daysBack}", { tickerIdx ->
        ret(tickerIdx)
    })
    return ret
}

fun Model.factorReturn(): (idx: Int) -> Double {
    val sers = enableSeries(Interval.Day, 5)

    val quantiles = quantiles(300)


    val ret = { tickerIdx: Int ->
        quantiles[tickerIdx].getQuantile(sers[tickerIdx][0].ret())
    }

    sers.forEachIndexed { idx, ts ->
        ts.preRollSubscribe {
            quantiles[idx].add(it[0].ret())
        }
    }

    enableFactor("returnCurrent", { tickerIdx ->
        val ret1 = ret(tickerIdx)
        if(ret1.isNaN()) -1.0 else ret1
    })
    return ret
}


fun Model.factorReturn(daysBack: Int): (idx: Int) -> Double {
    val sers = enableSeries(Interval.Day, daysBack + 5)
    val nonInterp = enableSeries(Interval.Day, daysBack + 5, false)
    val quantiles = quantiles(300)


    val ret = { tickerIdx: Int ->
        val metric =
            (sers[tickerIdx][0].close - nonInterp[tickerIdx][daysBack - 1].close) / nonInterp[tickerIdx][daysBack - 1].close
        quantiles[tickerIdx].getQuantile(metric)
    }

    nonInterp.forEachIndexed { idx, it ->
        it.preRollSubscribe {
            quantiles[idx].add(it.ret(daysBack))
        }
    }

    enableFactor("return${daysBack}", { tickerIdx ->
        val rr = ret(tickerIdx)
        if(rr.isNaN()) -1.0 else rr
    })
    return ret
}


fun Model.factorBarQuantLow(): (idx: Int) -> Double {
    val daytss = enableSeries(Interval.Day)
    val ret = { idx: Int ->
        daytss[idx][0].downShadow() / daytss[idx][0].range()
    }
    enableFactor("barQuantLow") {
        ret(it)
    }
    return ret
}

fun Model.factorAvgBarQuantLow(period: Int) {
    val daytss = enableSeries(Interval.Day)

    val mas = instruments().map { SimpleMovingAverage(period, false) }

    fun fac(idx: Int): Double {
        return daytss[idx][0].downShadow() / daytss[idx][0].range()
    }

    enableFactor("avgBarQuantLow${period}") {
        (mas[it].value() * period + fac(it)) / (period + 1.0)
    }

    daytss.forEachIndexed { idx, ts ->
        ts.preRollSubscribe {
            if (!it[0].interpolated) {
                mas[idx].add(fac(idx))
            }
        }
    }
}


fun Model.factorPoc(profiles: List<MarketProfile>, increments: DoubleArray) {
    val daytss = enableSeries(Interval.Day)
    val quantiles = quantiles(300)

    fun calcFactor(idx: Int): Double {
        try {
            return daytss[idx][0].close - profiles[idx].pocPrice * increments[idx]
        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0
        }

    }
    enableFactor("poc") {
        val ret = quantiles[it].getQuantile(calcFactor(it))
        if (ret.isFinite()) ret else -1.0
    }

    daytss.forEachIndexed { idx, it ->
        it.preRollSubscribe {
            if (!it[0].interpolated) {
                quantiles[idx].add(calcFactor(idx))
            }
        }
    }
}

fun Model.factorVolume(): (idx: Int) -> Double {
    val daytss = enableSeries(Interval.Day)
    val volumeQuantiles = quantiles(300)

    enableFactor("volume") {
        val ret = volumeQuantiles[it].getQuantile(daytss[it].last().volume.toDouble())
        if (ret.isNaN()) -1.0 else ret
    }

    daytss.forEachIndexed { idx, it ->
        it.preRollSubscribe {
            if (!it[0].interpolated) {
                volumeQuantiles[idx].add(it[0].volume.toDouble())
            }
        }
    }

    return { idx ->
        volumeQuantiles[idx].getQuantile(daytss[idx].last().volume.toDouble())
    }

}

fun Model.factorVolumeRelative(): (idx: Int) -> Double {
    val daytss = enableSeries(Interval.Day)
    val volumeQuantiles = quantiles(300)


    val ff = {idx : Int->
        val all =
            daytss.fold(BigDecimal.ZERO, { acc, ts -> acc.plus((ts.last().close.toBigDecimal().multiply(ts.last().volume.toBigDecimal()))) })
        (daytss[idx].last().volume.toDouble()*daytss[idx].last().close).toBigDecimal().divide(all, 5, RoundingMode.HALF_UP).toDouble()
    }

    enableFactor("relativeVolume") {
        val ret = volumeQuantiles[it].getQuantile(ff(it))
        if (ret.isNaN()) -1.0 else ret
    }

    daytss.forEachIndexed { idx, it ->
        it.preRollSubscribe {
            if (!it[0].interpolated) {
                volumeQuantiles[idx].add(ff(idx))
            }
        }
    }

    return { idx ->
        volumeQuantiles[idx].getQuantile(ff(idx))
    }

}



fun Model.factorMaDiff(period: Int = 30) {
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
            if (!it[0].interpolated) {
                quantiles[idx].add(it[0].close - mas[idx].value())
            }
        }
    }
}