package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.indicators.EmaSimple
import firelib.model.prod.*
import java.time.LocalDate

class SiStrat(context: ModelContext, val props: Map<String, String>) : Model(context, props) {
    init {

        val series = enableSeries(Interval.Min240, 100, false)

        val longEmas = series.map {
            EmaSimple(26, 0.0)
        }

        series.forEachIndexed { index, timeSeries ->
            timeSeries.preRollSubscribe {
                longEmas[index].onRoll(timeSeries[0].close)
            }
        }

        factorHour()
        factorWeekday()
        factorBarQuantLow()
        factorReturn(5)
        factorReturn(10)
        factorReturn(15)
        factorRank(5)


        series[0].preRollSubscribe {
            longEmas.forEachIndexed { idx, longEma ->
                if (series[idx][0].close > longEma.value()) {
                    if (position(idx) <= 0) {
                        flattenAll(idx)
                        longForMoneyIfFlat(idx, 1000_000)
                    }
                } else {
                    if (position(idx) >= 0) {
                        flattenAll(idx)
                        shortForMoneyIfFlat(idx, 1000_000)
                    }
                }
            }
        }
    }
}

fun siModel(): ModelConfig {
    return ModelConfig(SiStrat::class).apply {
        opt("ema.length", 12, 40, 2)
    }

}

fun main() {

//    UtilsHandy.updateTicker("Si", FinamDownloader.FUTURES_MARKET, Interval.Min10)

    siModel().runStrat(ModelBacktestConfig().apply {
        interval = Interval.Min10
        histSourceName = SourceName.FINAM
        instruments = listOf("Si")
        maxRiskMoneyPerSec = 1000_0000
        startDate(LocalDate.now().minusDays(1200))
        endDate(LocalDate.now().minusDays(300))
    })
}