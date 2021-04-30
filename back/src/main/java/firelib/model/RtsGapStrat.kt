package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.ret
import firelib.core.misc.atLondon
import firelib.core.misc.atMoscow
import firelib.core.timeseries.ret
import firelib.finam.FinamDownloader
import firelib.indicators.ATR
import firelib.model.prod.factorDay
import firelib.model.prod.factorReturn
import firelib.model.prod.factorReturnPct
import firelib.model.prod.factorWeekday
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class RtsGapStrat(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val intraTs = enableSeries(Interval.Min5, 100, false)[0]
        val dayTss = enableSeries(Interval.Day, 100)
        val dayTs = dayTss[0]

        val atr = ATR(26, enableSeries(Interval.Min15, 100, false)[0])

        var prevPrice = -1.0

        intraTs.preRollSubscribe {
            if (!it[0].interpolated && currentTime().atMoscow().toLocalTime() == LocalTime.of(19, 5)) {
                prevPrice = it[0].close
            }
        }

        closePosByCondition {
            currentTime().atMoscow().toLocalTime() >= LocalTime.of(18,0)
        }

        enableFactor("atr", {
            atr.value()
        })

        enableFactor("siRet", {
            dayTss[1][0].ret()
        })

        enableFactor("siGap", {
            (dayTss[1][0].open - dayTss[1][1].close) / dayTss[1][1].close
        })


        factorReturnPct()

        enableFactor("gap", {
            ((intraTs[0].close - prevPrice) / prevPrice)
        })

        factorWeekday()
        factorDay()

        factorReturn()
        factorReturn(2)
        factorReturn(3)
        factorReturn(4)


        intraTs.preRollSubscribe {
            val start = LocalTime.of(10, 0).plusMinutes(properties["start"]!!.toInt() * 5L)

            val moscowTime = currentTime().atMoscow()

            if (moscowTime.toLocalTime() == start && (moscowTime.dayOfWeek == DayOfWeek.MONDAY || moscowTime.dayOfWeek == DayOfWeek.FRIDAY)) {
                shortForMoneyIfFlat(0, 1000_000)
            }
        }
    }

    companion object {
        fun modelConfig(): ModelConfig {
            return ModelConfig(RtsGapStrat::class).apply {
                opt("start", 1, 40, 1)
            }
        }

    }
}

fun main() {
    RtsGapStrat.modelConfig().runStrat(ModelBacktestConfig().apply {
        interval = Interval.Min1
        instruments = listOf("RTS", "Si")
        histSourceName = SourceName.FINAM
        market = FinamDownloader.FinamMarket.FUTURES_MARKET.id
        startDate(LocalDate.now().minusDays(2500))
    })
}