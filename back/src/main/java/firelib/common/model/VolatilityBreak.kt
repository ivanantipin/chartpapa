package firelib.common.model

import com.funstat.finam.FinamDownloader
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.instruments
import firelib.common.config.runStrat
import firelib.common.core.ModelFactory
import firelib.common.interval.Interval
import firelib.common.misc.Quantiles
import firelib.common.misc.atUtc
import firelib.indicators.ATR
import firelib.indicators.Donchian
import java.time.LocalTime


/*
research model for simple breakout after low volatility period
 */

class VolatilityBreak(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    val period = 20

    val daytss = enableSeries(interval = Interval.Day,interpolated = false)

    val tenMins = enableSeries(interval = Interval.Min10,interpolated = false)

    val quantiles = context.tickers().map {
        Quantiles<Double>(1000);
    }
    val volumeQuantiles = context.tickers().map {
        Quantiles<Double>(100);
    }

    val donchians = daytss.map { Donchian({it.size <= period}) }


    val mas = daytss.mapIndexed { idx, it ->
        val atr = ATR(period, it)
        it.preRollSubscribe {
            quantiles[idx].add(atr.value())
        }
        atr
    }

    init {

        enableFactor("volatility", {
            val ret = quantiles[it].getQuantile(mas[it].value())
            if (ret.isNaN()) -1.0 else ret
        })
        enableFactor("volume", {
            val ret = volumeQuantiles[it].getQuantile(daytss[it].last().volume.toDouble())
            if (ret.isNaN()) -1.0 else ret
        })

        enableFactor("volume1", {
            val ret = volumeQuantiles[it].getQuantile(daytss[it][1].volume.toDouble())
            if (ret.isNaN()) -1.0 else ret
        })


        tenMins.forEachIndexed({idx,it->
            it.preRollSubscribe {
                val timeSeries = daytss[idx]
                if(it[0].endTime.atUtc().toLocalTime() > LocalTime.of(13,10) && timeSeries.count() > period){
                    val vola = quantiles[idx].getQuantile(mas[idx].value())
                    val vol = volumeQuantiles[idx].getQuantile(timeSeries.last().volume.toDouble())

                    if (it[0].close > donchians[idx].max) {
                        buyIfNoPosition(idx, 1000_000)
                    }
                }
            }
        })

        daytss.forEachIndexed({ idx, it ->
            it.preRollSubscribe {
                volumeQuantiles[idx].add(it[0].volume.toDouble())
                donchians[idx].add(it[0])
            }
        })

        closePositionByTimeout(periods = properties["hold_hours"]!!.toInt(), interval = Interval.Min60, afterTime = LocalTime.of(10,5))
    }



    companion object {

        val modelFactory : ModelFactory = { context, props ->
            VolatilityBreak(context, props)
        }

        fun modelConfig (waitOnEnd : Boolean = false , divAdjusted: Boolean = false) : ModelBacktestConfig{
            return ModelBacktestConfig().apply {
                reportTargetPath = "/home/ivan/projects/chartpapa/market_research/vol_break_report"
                param("hold_hours", 30)
                instruments = instruments(listOf("sber"),
                        source = FinamDownloader.SOURCE,
                        divAdjusted = divAdjusted,
                        waitOnEnd = waitOnEnd)
                adjustSpread = makeSpreadAdjuster(0.0005)
            }
        }

        fun runDefault(ctxListener : (Model)->Unit, waitOnEnd : Boolean = false , divAdjusted: Boolean = false){
            val conf = modelConfig(waitOnEnd,divAdjusted)
            conf.runStrat (modelFactory, ctxListener)
        }
    }
}

suspend fun main() {
    VolatilityBreak.runDefault ({  }, false)
}