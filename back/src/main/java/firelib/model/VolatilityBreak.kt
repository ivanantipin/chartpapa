package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.ModelFactory
import firelib.core.SimpleRunCtx
import firelib.core.backtest.Backtester
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.downShadow
import firelib.core.domain.range
import firelib.core.domain.upShadow
import firelib.core.enableOhlcDumping
import firelib.core.misc.Quantiles
import firelib.core.misc.UtilsHandy.updateRussianDivStocks
import firelib.core.misc.atMoscow
import firelib.core.misc.atUtc
import firelib.core.positionDuration
import firelib.model.VolatilityBreak.Companion.modelConfig
import firelib.indicators.ATR
import firelib.indicators.Donchian
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


/*
research model for simple breakout after low volatility period
 */

class VolatilityBreak(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    val period = 20

    val daytss = enableSeries(interval = Interval.Day, interpolated = false)

    val tenMins = enableSeries(interval = Interval.Min10, interpolated = false)

    val quantiles = context.config.instruments.map {
        Quantiles<Double>(100);
    }
    val volumeQuantiles = context.config.instruments.map {
        Quantiles<Double>(100);
    }

    val donchians = daytss.map { Donchian { it.size <= period } }


    val mas = daytss.mapIndexed { idx, it ->
        val atr = ATR(period, it)
        it.preRollSubscribe {
            quantiles[idx].add(atr.value())
        }
        atr
    }

    var allocated = 0

    init {

        enableFactor("volatility") {
            val ret = quantiles[it].getQuantile(mas[it].value())
            if (ret.isNaN()) -1.0 else ret
        }
        enableFactor("volume") {
            val ret = volumeQuantiles[it].getQuantile(daytss[it].last().volume.toDouble())
            if (ret.isNaN()) -1.0 else ret
        }

        enableFactor("volume1") {
            val ret = volumeQuantiles[it].getQuantile(daytss[it][1].volume.toDouble())
            if (ret.isNaN()) -1.0 else ret
        }

        enableFactor("barQuant") {
            daytss[it][0].upShadow()/daytss[it][0].range()
        }

        enableFactor("barQuantLow") {
            daytss[it][0].downShadow()/daytss[it][0].range()
        }

        enableFactor("hour") {
            currentTime().atMoscow().hour.toDouble()
        }


        tenMins.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                val timeSeries = daytss[idx]
                if (it[0].endTime.atMoscow().hour == 18 && timeSeries.count() > period )  {
                    val vola = quantiles[idx].getQuantile(mas[idx].value())
                    val vol = volumeQuantiles[idx].getQuantile(timeSeries.last().volume.toDouble())
                    if (it[0].close > donchians[idx].max && allocated < 3 && vola < 0.4 && currentTime().atMoscow().dayOfWeek != DayOfWeek.MONDAY) {
                        allocated += 1
                        longForMoneyIfFlat(idx, 1000_000)
                    }
                }
            }
        }

        daytss.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                volumeQuantiles[idx].add(it[0].volume.toDouble())
                donchians[idx].add(it[0])
            }
        }

        closePosByCondition { idx->
            val orderManager = orderManagers()[idx]
            val condition = (orderManager.position() != 0
                    && orderManager.positionDuration(currentTime()) > 24*3
                    && currentTime().atMoscow().toLocalTime().hour > 10
                    && tenMins[idx][0].close < donchians[idx].max
                    && !tenMins[idx][0].interpolated)
            if(condition){
                allocated -= 1
                println("position time ${orderManager.positionTime()} current time ${context.timeService.currentTime()}")
            }
            condition
        }
    }


    companion object {
        fun modelConfig(): ModelBacktestConfig {
            return ModelBacktestConfig(VolatilityBreak::class).apply {
                param("hold_hours", 30)
                interval = Interval.Min10
                startDate(LocalDate.now().minusDays(3000))
                instruments = DivHelper.getDivs().keys.toList()
//                adjustSpread = makeSpreadAdjuster(0.0005)
            }
        }
    }
}

fun defaultModelFactory(kl: KClass<out Model>): ModelFactory {
    val cons = kl.primaryConstructor!!
    return { a, b ->
        cons.call(a, b)
    }
}

fun main() {
    updateRussianDivStocks()
    val conf = modelConfig()
    println(conf.instruments)
    conf.dumpOhlc = true
    conf.runStrat()
}