package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atNy
import firelib.core.store.MdDaoContainer
import firelib.core.timeseries.makeUsTimeseries
import java.time.LocalDate
import kotlin.random.Random


class TrendModelUS(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        val usSeries = enableSeries(Interval.Min30, interpolated = false).map {
            makeUsTimeseries(it)
        }

        usSeries.forEachIndexed{idx, ts->
            ts.preRollSubscribe {
                if(ts[0].close > ts[1].close && ts[1].close > ts[2].close){
                    longForMoneyIfFlat(idx, 10_000)
                }
            }
        }

        enableSeries(Interval.Min30, interpolated = false).forEachIndexed {idx, ts->
            ts.preRollSubscribe {
                if(currentTime().atNy().hour <= 10 ){
                    flattenAll(idx)
                }
            }
        }
    }


    companion object{
        fun modelConfig(tradeSize : Int = 10_000): ModelConfig {
            return ModelConfig(TrendModelUS::class, ModelBacktestConfig().apply {
                //instruments = MdDaoContainer().getDao(SourceName.IQFEED, Interval.Min30).listAvailableInstruments()
                instruments =  sample(MdDaoContainer().getDao(SourceName.MT5, Interval.Min30).listAvailableInstruments(), 50)
                interval= Interval.Min30
                histSourceName = SourceName.MT5
                startDate(LocalDate.now().minusDays(600))
            }).apply {
                setTradeSize(tradeSize)
            }
        }
    }
}

fun sample(list : List<String>, n : Int) : List<String>{
    return (0 until n).map {
        Random.nextInt(list.size)
    }.map { list[it] }
}


fun main() {
    TrendModelUS.modelConfig().runStrat()
}