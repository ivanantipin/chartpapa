package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.report.dao.GeGeWriter
import firelib.indicators.SRMaker
import java.time.Instant
import java.time.LocalDate



data class support_resistance(val ticker : String, val initialDate : Instant, val startDate : Instant, val endDate : Instant, val level : Double)

class SRTrading(context: ModelContext, fac: Map<String, String>) : Model(context, fac) {
    val lst = mutableListOf<support_resistance>()

    init {
        val series = enableSeries(Interval.Min60, 5)
        val srmakers = instruments().map {ticker->
            val srMaker = SRMaker(numberOfExtremes = 20, numberOfHits = 2, zigZagMove = 0.03)
            srMaker.setEvictedListener {evicted->
                lst += evicted.map {sr-> support_resistance(ticker, sr.initial , sr.activeDate, currentTime(), sr.level) }
            }
            srMaker
        }

//        factorVolume()
//        factorHour()
//        factorWeekday()
//        factorBarQuantLow()

        enableSeries(Interval.Min240).forEachIndexed {idx, ts->
            ts.preRollSubscribe {
                val levels = srmakers[idx].currentLevels
                if(levels.any {sr->
                        it[0].close > sr.level &&
                                it[1].close < it[0].close &&
                                it[2].close < it[1].close
                    }){
                    longForMoneyIfFlat(idx, 100_000)
                }

//                if(levels.any {sr->
//                        it[0].close > sr.level &&
//                                it[1].close > it[0].close &&
//                                it[2].close > it[1].close &&
//                                it[0].low < sr.level
//                    }){
//                    longForMoneyIfFlat(idx, 100_000)
//                }
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

    override fun onBacktestEnd() {
        super.onBacktestEnd()
        val writer = GeGeWriter<support_resistance>(
            runConfig().getReportDbFile(),
            support_resistance::class
        )
        writer.write(lst)
    }

    companion object {
        fun modelConfig(): ModelConfig {
            return ModelConfig(SRTrading::class)
        }
    }
}

fun main() {
    SRTrading.modelConfig().runStrat(ModelBacktestConfig().apply {
        instruments = listOf("USDRUB.c")
        interval= Interval.Min15
        startDate(LocalDate.now().minusDays(5000))
        histSourceName = SourceName.MT5
    })
}
