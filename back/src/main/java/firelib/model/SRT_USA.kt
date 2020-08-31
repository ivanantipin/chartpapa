package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.store.MdDaoContainer
import firelib.indicators.SRMaker
import java.time.LocalDate


class SRT_USA(context: ModelContext, fac: Map<String, String>) : Model(context, fac) {


    init {
        val series = enableSeries(Interval.Min60, 5)
        val srmakers = instruments().map {ticker->
            val srMaker = SRMaker(0.002, numberOfExtremes = 20, numberOfHits = 3, zigZagMove = 0.03)
            srMaker
        }

        enableSeries(Interval.Min240).forEachIndexed {idx, ts->
            ts.preRollSubscribe {
                if(!it[0].interpolated){
                    val levels = srmakers[idx].currentLevels
                    if(levels.any {sr->
                            it[0].close > sr.level &&
                                    it[1].close < it[0].close &&
                                    it[2].close < it[1].close
                        }){
                        longForMoneyIfFlat(idx, 100_000)
                    }

                }
            }
        }

        closePosByCondition {
            !series[it][0].interpolated && positionDuration(it) > 2 * 24
        }

        instruments().forEachIndexed({idx, ticker->
            series[idx].preRollSubscribe {
                if(!it[0].interpolated){
                    srmakers[idx].addOhlc(it[0])
                }
            }
        })

    }

    companion object {
        fun modelConfig(): ModelConfig {
            return ModelConfig(SRT_USA::class)
        }
    }
}

fun main() {
    SRT_USA.modelConfig().runStrat(ModelBacktestConfig().apply {
        instruments = sample(MdDaoContainer().getDao(SourceName.IQFEED, Interval.Min30).listAvailableInstruments(), 100)
        interval= Interval.Min30
        startDate(LocalDate.now().minusDays(600))
        histSourceName = SourceName.IQFEED
    })
}
