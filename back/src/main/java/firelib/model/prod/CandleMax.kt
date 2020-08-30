package firelib.model.prod

import firelib.core.*
import firelib.core.config.*
import firelib.core.domain.*
import firelib.core.misc.atMoscow
import firelib.model.tickers
import java.time.DayOfWeek
import java.time.LocalDate


class CandleMax(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    val daytss = enableSeries(interval = Interval.Day)

    val rootInterval = enableSeries(interval = Interval.Min1)

    init {
        val volRel = factorVolumeRelative()
        val rankFunk = factorRank(33)
        val ret = factorReturn()
        rootInterval[0].preRollSubscribe {
            instruments().forEachIndexed { idx, _ ->
                val moscomTime = currentTime().atMoscow()
                if (moscomTime.hour == 18 && moscomTime.minute == 42
                    && volRel(idx) > 0.8
                    && moscomTime.dayOfWeek != DayOfWeek.THURSDAY
                    && rankFunk(idx) > 5
                    && !daytss[0][0].interpolated
                ) {
                    if (ret(idx) > 0.92) {
                        longForMoneyIfFlat(idx, tradeSize())
                    }
                }
            }
        }

        closePosByCondition { idx ->
            val moscomTime = currentTime().atMoscow()
            val orderManager = orderManagers()[idx]
            (orderManager.position() != 0
                    && orderManager.positionDuration(currentTime()) > 10
                    && !daytss[idx][0].interpolated
                    && moscomTime.hour == 18
                    && moscomTime.minute == 30
                    )
        }
    }


    companion object {
        fun runConfig(): ModelBacktestConfig {
            return ModelBacktestConfig().apply {
                instruments = tickers.filter { it != "irao" }
                interval = Interval.Min1
                maxRiskMoney = 1000_000
                histSourceName = SourceName.FINAM
                startDate(LocalDate.now().minusDays(1700))
            }
        }

        fun modelConfig(tradeSize : Int): ModelConfig {
            return ModelConfig(
                CandleMax::class,
                runConfig()
                //commonRunConfig()
            ).apply {
                setTradeSize(tradeSize)
            }
        }
    }
}

fun main() {
    val conf = CandleMax.modelConfig(100_000)
    conf.runStrat()
}