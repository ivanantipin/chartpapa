package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.enableDivs
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import java.lang.Exception
import java.time.Instant
import java.time.LocalDate


class RealDivModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        var divMap = context.config.tickerToDiv!!

        val tss = enableSeries(Interval.Min10, interpolated = false)

        context.mdDistributor.addListener(Interval.Week) {a,b->
            if(Instant.now().epochSecond - currentTime().epochSecond < 5000){
                try {
                    divMap = OpenDivHelper.fetchDivs(LocalDate.now().minusDays(1)).groupBy { it.ticker.toLowerCase() }
                    log.info("divs updated")
                }catch (e : Exception){
                    log.info("error updating divs", e)
                }

            }
        }

        context.config.instruments.forEachIndexed { idx, instrument ->
            tss[idx].preRollSubscribe {

                val divs = divMap[instrument]!!.associateBy { it.lastDayWithDivs }

                val localTime = currentTime().atMoscow()

                val date = localTime.toLocalDate()

                if (localTime.hour == 18 && localTime.minute == 30 && divs.containsKey(date)) {
                    longForMoneyIfFlat(idx, 100_000)
                }else if (localTime.hour == 18 && localTime.minute == 20 && position(idx) != 0) {
                    flattenAll(idx)
                }
            }
        }
    }
}

fun main() {
    val divMap = OpenDivHelper.fetchDivs(LocalDate.now().minusDays(1300)).groupBy { it.ticker.toLowerCase() }
    val conf = ModelBacktestConfig(RealDivModel::class).apply {
        instruments = divMap.keys.toList()
        startDate(LocalDate.now().minusDays(1300))
        enableDivs(divMap)
    }
    conf.runStrat()
}
