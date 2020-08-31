package firelib.model.prod

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.*
import firelib.core.misc.atMoscow
import firelib.indicators.Donchian
import firelib.model.*
import firelib.model.prod.VolatilityBreak.Companion.modelConfig
import java.time.LocalDate


class VolatilityBreak(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {
    val period = 20
    val daytss = enableSeries(interval = Interval.Day)
    val minSeries = enableSeries(interval = Interval.Min1)
    val donchians = daytss.map { Donchian { it.size <= period } }

    init {
        val tradeSize = properties["trade_size"]!!.toInt()
        minSeries.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                val dayTs = daytss[idx]
                if (!dayTs[0].interpolated && currentTime().atMoscow().hour == 18 && currentTime().atMoscow().minute == 42 &&  dayTs.count() > period )  {
                    val barQuantLow = barQuantLowFun(idx)
                    logRealtime { "checking condition ${instruments()[idx]} is ${donchians[idx].max} vs close ${it[0].close} bar quant ${barQuantLow}"}
                    if (it[0].close > donchians[idx].max && barQuantLow > 0.8) {
                        longForMoneyIfFlat(idx, tradeSize.toLong())
                    }

                    if((position(idx) != 0 &&
                                positionDuration(idx) > 25*2
                                && dayTs[0].close < donchians[idx].max)){
                        flattenAll(idx)
                    }
                }
            }
        }

        daytss.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                if(!it[0].interpolated){
                    donchians[idx].add(it[0])
                    logRealtime { "donchian for ticker ${instruments()[idx]} is ${donchians[idx].min} - ${donchians[idx].max}"}
                }
            }
        }
    }
    private fun barQuantLowFun(it: Int) = daytss[it][0].downShadow() / daytss[it][0].range()
    companion object {
        fun modelConfig(tradeSize : Int): ModelConfig {
            return ModelConfig(VolatilityBreak::class).apply {
                param("hold_hours", 30)
                setTradeSize(tradeSize)
            }
        }
    }
}


fun main() {
    val conf = modelConfig(250_000)
    //conf.runConfig.dumpInterval = Interval.Day
    conf.runStrat(ModelBacktestConfig().apply {
        interval = Interval.Min1
        histSourceName = SourceName.FINAM
        startDate(LocalDate.now().minusDays(1500))
        instruments = tickers
    })
}