package firelib.model.prod

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.model.tickers
import java.time.LocalDate


class TrendModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    val daytss = enableSeries(Interval.Day)

    val nonInterpolated = enableSeries(Interval.Day, interpolated = false)

    init {
        val back = props["period"]!!.toInt()

        enableSeries(Interval.Min1)[0].preRollSubscribe {
            if (daytss[0].count() > 40 && currentTime().atMoscow().hour == 18 && !daytss[0][0].interpolated) {

                if(currentTime().atMoscow().minute == 41){

                    val num = props["number"]!!.toInt()
                    val idxToRet = daytss.mapIndexed { idx, ts ->
                        var ret = (daytss[idx][0].close - nonInterpolated[idx][back].close) / nonInterpolated[idx][back].close
                        if(position(idx) > 0){
                            ret += 0.005
                        }
                        Pair(idx, ret)
                    }

                    val indexed = idxToRet.filter { it.second.isFinite() && it.second > 0 }

                    val sortedBy = indexed.sortedBy { -it.second }

                    val sorted = sortedBy.subList(0, Math.min(num, indexed.size)).map { it.first }

                    idxToRet.forEach {
                        logRealtime { "return for ticker ${instruments()[it.first]} is ${it.second}"}
                    }

                    logRealtime{"====="}

                    logRealtime { ("top is ${sorted.map{instruments()[it]}}")}


                    instruments().mapIndexed{idx, _->idx}.filter { !sorted.contains(it) }.forEach {
                        flattenAll(it)
                    }

                    sorted.forEach {
                        longForMoneyIfFlat(it, tradeSize())
                    }
                }
            }
        }
    }
    companion object{
        fun modelConfig(tradeSize : Int = 10_000): ModelConfig {
            return ModelConfig(TrendModel::class).apply {
                setTradeSize(tradeSize)
                param("period", 33)
                param("number", 5)
            }
        }
    }
}


fun main() {
    TrendModel.modelConfig().runStrat(ModelBacktestConfig().apply {
        instruments = tickers
        interval = Interval.Min1
        histSourceName = SourceName.FINAM
        startDate(LocalDate.now().minusDays(300))
    })
}