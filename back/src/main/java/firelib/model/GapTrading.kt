package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.report.dao.GeGeWriter
import java.time.LocalDate


class GapTrading(context: ModelContext, fac: Map<String, String>) : Model(context, fac) {

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
        val dayRolled = instruments().map { false }.toMutableList()
        val tssDay = enableSeries(Interval.Day)
        tssDay.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                if (!it[0].interpolated) {
                    dayRolled[idx] = true
                }
            }
        }

        instruments().forEachIndexed { idx, tick ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Min60, 1000)
            ret.preRollSubscribe {
                if (dayRolled[idx] && it[1].interpolated && !it[0].interpolated) {
                    dayRolled[idx] = false
                    val gap = (it[0].open - tssDay[idx][1].close) / tssDay[idx][1].close
                    if (gap < -0.02) {
                        shortForMoneyIfFlat(idx, -1000_000)
                    }
                    log.info("written ${it[0].endTime} ${tssDay[idx][1].endTime} ")
                }
            }
            ret
        }

        closePositionByTimeout(periods = properties["holdtime"]!!.toInt(), interval = Interval.Min60)

    }

    override fun onBacktestEnd() {
        super.onBacktestEnd()
        val writer = GeGeWriter<GapStat>(
            runConfig().getReportDbFile(),
            GapStat::class,
            name = "gaps"
        )
        writer.write(stat)
    }

    companion object {
        fun modelConfig(): ModelConfig {
            return ModelConfig(GapTrading::class, ModelBacktestConfig().apply {
                instruments = tickers
                startDate(LocalDate.now().minusDays(5000))
            }).apply {
                opt("holdtime", 1, 3, 1)
            }
        }

    }


}

fun main() {
    GapTrading.modelConfig().runStrat()
}
