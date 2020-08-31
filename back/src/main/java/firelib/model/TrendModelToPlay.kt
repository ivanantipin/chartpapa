package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.indicators.VWAP
import java.time.LocalDate


class TrendModelToPlay(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    val daytss = enableSeries(Interval.Day)

    val nonInterpolated = enableSeries(Interval.Day, interpolated = false)

    init {
        val back = props["period"]!!.toInt()
        val num = props["number"]!!.toInt()

        val vwaps = instruments().mapIndexed {idx, _->
            val vwap = VWAP(back)
            nonInterpolated[idx].preRollSubscribe {
                vwap.addOhlc(it[0])
            }
            vwap
        }

        enableSeries(Interval.Min60, interpolated = false)[0].preRollSubscribe {
            if (daytss[0].count() > 40 && currentTime().atMoscow().hour == 18) {

                val idxToRet = daytss.mapIndexed { idx, ts ->
                    var ret = (nonInterpolated[idx][0].close - nonInterpolated[idx][back].close) / nonInterpolated[idx][back].close
                    var vwpaDiff = (nonInterpolated[idx][0].close - vwaps[idx].value()) / vwaps[idx].value()
                    if(position(idx) > 0){
                        ret += 0.005
                    }
                    Triple(idx, ret,vwpaDiff)
                }

                val indexed = idxToRet.filter { it.second.isFinite() && it.second > 0 }

                val sortedBy = indexed.sortedBy { -it.second }

                val sorted = sortedBy
                    .subList(0, Math.min(num, indexed.size))
                    .sortedBy { it.third }
                    .subList(0, Math.min(num/2, indexed.size))
                    .map { it.first }


                idxToRet.forEach {
                    logRealtime { "return for ticker ${instruments()[it.first]} is ${it.second}"}
                }

                logRealtime{"====="}

                logRealtime { ("top is ${sorted.map{instruments()[it]}}")}


                oms.forEachIndexed { idx, om ->
                    if (sorted.contains(idx)) {
                        longForMoneyIfFlat(idx, tradeSize())
                    } else {
                        om.flattenAll()
                    }
                }

            }
        }
    }
    companion object{
        fun modelConfig(tradeSize : Int = 10_000): ModelConfig {
            return ModelConfig(TrendModelToPlay::class).apply {
                setTradeSize(tradeSize)
                param("period", 33)
                param("number", 10)
            }
        }
    }
}


fun main() {
    TrendModelToPlay.modelConfig().runStrat(ModelBacktestConfig().apply {
        instruments = tickers
        startDate(LocalDate.now().minusDays(3000))
    })
}