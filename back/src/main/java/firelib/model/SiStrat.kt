package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.indicators.Ema
import firelib.model.prod.factorHour
import java.time.LocalDate


class SiStrat(context: ModelContext, val props: Map<String, String>) : Model(context, props) {
    init {
        val intraTs = enableSeries(Interval.Min30, 100, false)[0]
        val dayTs = enableSeries(Interval.Day, interpolated = true)[0]

        val shortEma = Ema(12, dayTs)
        val longEma = Ema(26, dayTs)

        factorHour()

        intraTs.preRollSubscribe {
            if (longEma.value() < shortEma.value()) {
                if (position(0) <= 0) {
                    flattenAll(0)
                    longForMoneyIfFlat(0, 1000_000)
                }

            } else {
                if (position(0) >= 0) {
                    flattenAll(0)
                    shortForMoneyIfFlat(0, 1000_000)
                }
            }
        }
    }
}

fun siModel(): ModelConfig {
    return ModelConfig(SiStrat::class).apply {
        param("hold_hours", 30)

    }

}

fun main() {

//    UtilsHandy.updateTicker("Si", FinamDownloader.FUTURES_MARKET, Interval.Min10)

    siModel().runStrat(ModelBacktestConfig().apply {
        interval = Interval.Min10
        histSourceName = SourceName.FINAM
        instruments = listOf("SI")
        maxRiskMoneyPerSec = 1000_0000
        startDate(LocalDate.now().minusDays(5000))
    })
}