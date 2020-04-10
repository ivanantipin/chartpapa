package firelib.model.prod

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.domain.downShadow
import firelib.core.domain.range
import firelib.core.domain.upShadow
import firelib.core.misc.Quantiles
import firelib.core.misc.atMoscow
import firelib.core.positionDuration
import firelib.indicators.ATR
import firelib.indicators.Donchian
import firelib.model.*
import firelib.model.prod.VolatilityBreak.Companion.modelConfig
import java.time.Instant
import java.time.LocalDate


class VolatilityBreak(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    val period = 20

    val daytss = enableSeries(interval = Interval.Day)

    val tenMins = enableSeries(interval = Interval.Min10, interpolated = false)

    val quantiles = context.config.instruments.map {
        Quantiles<Double>(300);
    }
    val volumeQuantiles = context.config.instruments.map {
        Quantiles<Double>(300);
    }

    val donchians = daytss.map { Donchian { it.size <= period } }


    val mas = daytss.mapIndexed { idx, it ->
        val atr = ATR(period, it)
        it.preRollSubscribe {
            quantiles[idx].add(atr.value())
        }
        atr
    }

    init {
        val tradeSize = properties["trade_size"]!!.toInt()

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
            barQuantLowFun(it)
        }

        enableFactor("hour") {
            currentTime().atMoscow().hour.toDouble()
        }

        enableSeries(Interval.Min10)[0].preRollSubscribe {
            if(Instant.now().epochSecond - currentTime().epochSecond < 100){
                println("ohlc ${context.config.instruments[0]} -- ${it[0]}")
            }
        }


        tenMins.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                val timeSeries = daytss[idx]
                if (it[0].endTime.atMoscow().hour == 18 && timeSeries.count() > period )  {
                    val vola = quantiles[idx].getQuantile(mas[idx].value())
                    val vol = volumeQuantiles[idx].getQuantile(timeSeries.last().volume.toDouble())
                    val vol1 = volumeQuantiles[idx].getQuantile(timeSeries[1].volume.toDouble())
                    val barQuantLow = barQuantLowFun(idx)
                    logRealtime { "checking condition ${instruments()[idx]} is ${donchians[idx].max} vs close ${it[0].close} bar quant ${barQuantLow}"}
                    if (it[0].close > donchians[idx].max && barQuantLow > 0.8) {
                        longForMoneyIfFlat(idx, tradeSize.toLong())
                    }
                }
            }
        }

        daytss.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                if(!it[0].interpolated){
                    volumeQuantiles[idx].add(it[0].volume.toDouble())
                    donchians[idx].add(it[0])

                    logRealtime { "donchian for ticker ${instruments()[idx]} is ${donchians[idx].min} - ${donchians[idx].max}"}

                }
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
                println("position time ${orderManager.positionTime()} current time ${context.timeService.currentTime()}")
            }
            condition
        }
    }

    private fun barQuantLowFun(it: Int) = daytss[it][0].downShadow() / daytss[it][0].range()


    companion object {
        fun modelConfig(tradeSize : Int): ModelBacktestConfig {
            return ModelBacktestConfig(VolatilityBreak::class).apply {
                param("hold_hours", 30)
                setTradeSize(tradeSize)
                interval = Interval.Min10
                startDate(LocalDate.now().minusDays(3000))
                instruments = tickers
            }
        }
    }
}


fun main() {
    val conf = modelConfig(1000_000)
    conf.dumpInterval = Interval.Day
    conf.runStrat()
}