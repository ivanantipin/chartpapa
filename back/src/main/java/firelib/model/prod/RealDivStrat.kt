package firelib.model.prod

import firelib.core.*
import firelib.core.config.*
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.model.*
import java.time.Instant
import java.time.LocalDate


class RealDivModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        var divMap = runConfig().tickerToDiv ?: emptyMap()

        val tss = enableSeries(Interval.Min10, interpolated = false)

        context.mdDistributor.addListener(Interval.Week) {a,b->
            if(Instant.now().epochSecond - currentTime().epochSecond < 5000){
                try {
                    divMap = mapOf()
                    log.info("divs updated ${divMap}")
                }catch (e : Exception){
                    log.info("error updating divs", e)
                }
            }
        }

        instruments().forEachIndexed { idx, instrument ->

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
                }

                if (localTime.hour == 18 && localTime.minute == 20) {
                    logRealtime { "checking if ${instrument} has position " }
                    if(position(idx) != 0){
                        flattenAll(idx)
                    }
                }
            }
        }
    }
    companion object{
        fun modelConfig(tradeSize : Int = 10_000) : ModelConfig{
            return ModelConfig(RealDivModel::class).apply {
                setTradeSize(tradeSize)
            }
        }
    }
}

fun commonRunConfig() : ModelBacktestConfig{
    val divMap = mapOf<String, List<Div>>()
    return ModelBacktestConfig().apply {
        instruments = tickers
        interval = Interval.Min1
        histSourceName = SourceName.FINAM
        startDate(LocalDate.now().minusDays(300))
        enableDivs(divMap)
    }
}

fun runCfgWODivs() : ModelBacktestConfig{

    return ModelBacktestConfig().apply {
        instruments = tickers
        interval = Interval.Min1
        histSourceName = SourceName.FINAM
        startDate(LocalDate.now().minusDays(1500))
    }
}


fun main() {
    RealDivModel.modelConfig().runStrat(commonRunConfig())
}
