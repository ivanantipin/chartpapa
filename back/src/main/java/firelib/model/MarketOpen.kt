package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.report.dao.GeGeWriter
import firelib.core.domain.ret


class MarketOpen(context: ModelContext, val fac: Map<String, String>) : Model(context, fac) {

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

        val dayRolled = context.config.instruments.map { false }.toMutableList()

        val tssDay = context.config.instruments.mapIndexed { idx, tick ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Day, 10)
            ret.preRollSubscribe {
                if (!ret[0].interpolated) {
                    dayRolled[idx] = true
                }
            }
            ret
        }

        context.config.instruments.forEachIndexed { idx, tick ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Min60, 1000)
            ret.preRollSubscribe {
                if (dayRolled[idx] && it[5].interpolated && !it[4].interpolated) {
                    dayRolled[idx] = false

                    stat.add(
                        GapStat(
                            ticker = tick,
                            gapPct = (it[4].open - tssDay[idx][1].close) / tssDay[idx][1].close,
                            h0 = it[4].ret(),
                            h1 = it[3].ret(),
                            h2 = it[2].ret(),
                            h3 = it[1].ret(),
                            h4 = it[0].ret()

                        )
                    )

                    log.info("written ${it[0].endTime} ${tssDay[idx][1].endTime} ")
                }
            }
            ret
        }
    }

    override fun onBacktestEnd() {
        super.onBacktestEnd()
        val writer = GeGeWriter<GapStat>(
            "gaps",
            context.config.getReportDbFile(),
            GapStat::class
        )
        writer.write(stat)
    }

}

fun main() {
    val conf = ModelBacktestConfig(MarketOpen::class).apply {
        instruments = DivHelper.getDivs().keys.toList()
    }
    conf.runStrat()
}
