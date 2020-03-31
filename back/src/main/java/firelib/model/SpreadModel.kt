package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.misc.UtilsHandy
import firelib.core.flattenAll
import firelib.core.makePositionEqualsTo
import firelib.core.timeseries.TimeSeries
import firelib.core.domain.Ohlc
import firelib.core.misc.Quantiles
import firelib.core.misc.atMoscow
import firelib.core.store.MdStorageImpl
import firelib.core.store.finamMapperWriter
import java.time.LocalDate
import kotlin.math.abs

class SpreadModel(context: ModelContext, val props: Map<String, String>) : Model(context,props){

    val period = props["period"]!!.toInt()

    init {
        val tss = enableSeries(Interval.Min10,100, false)

        val quantiles = Quantiles<Double>(200);

        val ts0 = tss[0]
        val ts1 = tss[1]


        ts0.preRollSubscribe {

            val r0 = (ts0[0].close - ts0[period].close) / ts0[period].close
            val r1 = (ts1[0].close - ts1[period].close) / ts1[period].close


            val spread = r0 - r1

            val q = quantiles.getQuantile(spread)

            println("quantile is ${q}")

            if(q > 0.95){
//                shortForMoneyIfFlat(0, 100_000)
                longForMoneyIfFlat(1, 5000)
                flattenAll(0)
            }else if(q < 0.05){
                longForMoneyIfFlat(0, 5000)
                flattenAll(1)
//                shortForMoneyIfFlat(1, 100_000)
            }else{
                flattenAll(0)
                flattenAll(1)
            }

            quantiles.add(spread)
        }

    }
}

fun spreadModel(): ModelBacktestConfig {
    return ModelBacktestConfig(SpreadModel::class).apply{
        startDate(LocalDate.now().minusDays(1500))
        param("period", 10)
//        opt("period", 3,20, 1)
        instruments = listOf("sber", "sberp")
    }
}

fun main() {
    spreadModel().runStrat()
}