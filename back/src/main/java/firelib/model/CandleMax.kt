package firelib.model

import firelib.core.*
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.*
import firelib.core.misc.Quantiles
import firelib.core.misc.atMoscow
import firelib.indicators.ATR
import firelib.model.prod.commonRunConfig


/*
research model for simple breakout after low volatility period
 */

class CandleMax(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    val period = 20

    val daytss = enableSeries(interval = Interval.Day)

    val tenMins = enableSeries(interval = Interval.Min10, interpolated = false)

    val quantiles =  quantiles(100)

    val volumeQuantiles = quantiles(100)

    val mas = daytss.mapIndexed { idx, it ->
        val atr = ATR(period, it)
        it.preRollSubscribe {
            quantiles[idx].add(atr.value())
        }
        atr
    }


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
                val moscomTime = currentTime().atMoscow()
                if (moscomTime.hour == 18 && moscomTime.minute == 30)  {
                    if(daytss[idx][0].ret() > 0 && daytss[idx][0].upShadow()/daytss[idx][0].retAbs() < 0.05){
                        longForMoneyIfFlat(idx, 1000_000)
                    }
                }
            }
        }

        daytss.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                volumeQuantiles[idx].add(it[0].volume.toDouble())
            }
        }

        closePosByCondition { idx->
            val moscomTime = currentTime().atMoscow()
            val orderManager = orderManagers()[idx]
            val condition = (orderManager.position() != 0
                    && orderManager.positionDuration(currentTime()) > 10
                    && !tenMins[0][0].interpolated
                    && moscomTime.hour == 18 && moscomTime.minute == 30
                    )
            if(condition){
                println("position time ${orderManager.positionTime()} current time ${context.timeService.currentTime()}")
            }
            condition
        }
    }


    companion object {
        fun modelConfig(): ModelConfig {
            return ModelConfig(CandleMax::class, commonRunConfig()).apply {
                param("hold_hours", 30)
            }
        }
    }
}


fun main() {
    //updateRussianDivStocks()
    val conf = CandleMax.modelConfig()
    conf.runConfig.dumpInterval = Interval.Day
    conf.runStrat()
}