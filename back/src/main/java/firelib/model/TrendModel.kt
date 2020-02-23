package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.flattenAll
import firelib.core.misc.atMoscow
import java.time.DayOfWeek
import java.time.LocalDate

class TrendModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val tss = enableSeries(Interval.Day, interpolated = false)

        context.mdDistributor.addListener(Interval.Day) { _, _ ->
            if (tss[0].count() > 40 && currentTime().atMoscow().dayOfWeek == DayOfWeek.MONDAY) {

                val back = props["period"]!!.toInt()

                val indexed = tss.mapIndexed({ idx, ts -> Pair(idx, (ts[0].close - ts[back].close) / ts[back].close) }).filter { it.second.isFinite() }

                val sorted = indexed.sortedBy { it.second }



                sorted.forEachIndexed { idx, pair ->
                    if (idx > sorted.size - 5 && pair.second > 0) {
                        longForMoneyIfFlat(pair.first, 1000_000)
                    } else {
                        oms[pair.first].flattenAll()
                    }
                }
            }
        }

    }
}

fun main() {
    val conf = ModelBacktestConfig(TrendModel::class).apply {
        instruments = DivHelper.getDivs().keys.toList().filter { it != "irao" }
        startDate(LocalDate.now().minusDays(3000))
        param("period", 37)
//        opt("period", 5, 45, 2)
    }
    conf.runStrat()
}
