package firelib.model.prod

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.enableDivs
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.model.*
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
                    log.info("divs updated ${divMap}")
                }catch (e : Exception){
                    log.info("error updating divs", e)
                }
            }
        }

        context.config.instruments.forEachIndexed { idx, instrument ->

            var counter = 0

            tss[idx].preRollSubscribe {

                if(++counter % 10 == 0){
                    logRealtime { "i am alive ${instrument} -  ${it[0]}" }
                }

                val divs = divMap.getOrDefault(instrument, emptyList()).associateBy { it.lastDayWithDivs }

                val localTime = currentTime().atMoscow()

                val date = localTime.toLocalDate()

                if (localTime.hour == 18 && localTime.minute == 30 && divs.containsKey(date)) {
                    logRealtime { "long for ${instrument} as it has a div ${divs[date]}" }
                    longForMoneyIfFlat(idx, tradeSize())
                }else if (localTime.hour == 18 && localTime.minute == 20 && position(idx) != 0) {
                    flattenAll(idx)
                }
            }
        }
    }
    companion object{
        fun modelConfig(tradeSize : Int = 10_000) : ModelBacktestConfig{
            val divMap = OpenDivHelper.fetchDivs(LocalDate.now().minusDays(1300)).groupBy { it.ticker.toLowerCase() }
            return ModelBacktestConfig(RealDivModel::class).apply {
                instruments = divMap.keys.toList()
                setTradeSize(tradeSize)
                startDate(LocalDate.now().minusDays(1300))
                enableDivs(divMap)
            }
        }
    }
}

fun main() {
    RealDivModel.modelConfig().runStrat()
}
