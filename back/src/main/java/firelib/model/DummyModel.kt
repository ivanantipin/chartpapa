package firelib.model

import firelib.core.Model
import firelib.core.ModelContext
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.ret
import firelib.core.enableSeries
import firelib.core.instruments
import firelib.core.report.dao.GeGeWriter
import java.time.LocalDate


class DummyModel(context: ModelContext, val fac: Map<String, String>) : Model(context, fac) {

    val stat = mutableListOf<GapStat>()

    data class GapStat(
        val ticker: String, val gapPct: Double,
        val h0: Double,
        val h1: Double,
        val h2: Double,
        val h3: Double,
        val h4: Double
    )

    init {
        enableSeries(Interval.Sec10)[0].preRollSubscribe {
            println(it[0])
        }
    }

    override fun onBacktestEnd() {
        super.onBacktestEnd()
        val writer = GeGeWriter<GapStat>(
            context.config.runConfig.getReportDbFile(),
            GapStat::class,
            name = "gaps"
        )
        writer.write(stat)
    }

    companion object {
        fun modelConfig(): ModelConfig {
            return ModelConfig(DummyModel::class, ModelBacktestConfig().apply {
                interval = Interval.Sec10
                instruments = listOf("RIM0")
                disableBacktest = true
                startDate(LocalDate.now().minusDays(5000))
            })
        }

    }


}

fun main() {
    MarketOpen.modelConfig().runStrat()
}
