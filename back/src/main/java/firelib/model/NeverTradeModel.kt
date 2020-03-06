package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.domain.Interval
import java.time.LocalDate

class NeverTradeModel(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    init {
        enableSeries(Interval.Sec10)[0].preRollSubscribe {
            println(it[0])
        }
    }

    companion object {
        fun modelConfig(): ModelBacktestConfig {
            val cfg = ModelBacktestConfig(NeverTradeModel::class).apply {
                instruments = listOf("SRH0")
                startDate(LocalDate.now().minusDays(1))
                interval = Interval.Sec10
            }
            return cfg
        }
    }
}

class SingleTradeModel(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    init {
        enableSeries(Interval.Sec10)[0].preRollSubscribe {
            println("ohlc ${it[0]}")
            longForMoneyIfFlat(0,24000)
        }
    }

    companion object {
        fun modelConfig(): ModelBacktestConfig {
            val cfg = ModelBacktestConfig(SingleTradeModel::class).apply {
                instruments = listOf("SRH0")
                disableBacktest = true
                interval = Interval.Sec10
            }
            return cfg
        }
    }
}