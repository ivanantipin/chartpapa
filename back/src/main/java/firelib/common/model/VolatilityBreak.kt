package firelib.common.model

import firelib.common.config.ModelBacktestConfig
import firelib.common.core.ModelFactory
import firelib.common.core.SimpleRunCtx
import firelib.common.interval.Interval
import firelib.common.misc.Quantiles
import firelib.common.misc.atUtc
import firelib.common.model.VolatilityBreak.Companion.modelConfig
import firelib.indicators.ATR
import firelib.indicators.Donchian
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
        Quantiles<Double>(1000);
    }
    val volumeQuantiles = context.config.instruments.map {
        Quantiles<Double>(100);
    }

    val donchians = daytss.map { Donchian({ it.size <= period }) }


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


        tenMins.forEachIndexed({ idx, it ->
            it.preRollSubscribe {
                val timeSeries = daytss[idx]
                if (it[0].endTime.atUtc().toLocalTime() > LocalTime.of(13, 10) && timeSeries.count() > period) {
                    val vola = quantiles[idx].getQuantile(mas[idx].value())
                    val vol = volumeQuantiles[idx].getQuantile(timeSeries.last().volume.toDouble())

                    if (it[0].close > donchians[idx].max) {
                        longForMoneyIfFlat(idx, 1000_000)
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

        closePositionByTimeout(periods = properties["hold_hours"]!!.toInt(), interval = Interval.Min60, afterTime = LocalTime.of(10, 5))
    }


    companion object {
        fun modelConfig(waitOnEnd: Boolean = false, divAdjusted: Boolean = false): ModelBacktestConfig {
            return ModelBacktestConfig(VolatilityBreak::class).apply {
                param("hold_hours", 30)
                interval = Interval.Min10
                startDate(LocalDate.now().minusDays(100))
                instruments = DivHelper.getDivs().keys.toList()
                adjustSpread = makeSpreadAdjuster(0.0005)
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
    val conf = modelConfig(false)
    val fac = defaultModelFactory(VolatilityBreak::class)

    val ctx = SimpleRunCtx(VolatilityBreak.modelConfig())
    ctx.addModel(emptyMap())

}