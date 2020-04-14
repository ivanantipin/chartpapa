package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.misc.Quantiles
import firelib.core.store.DbReaderFactory
import java.time.LocalDate

class SpreadModel(context: ModelContext, val props: Map<String, String>) : Model(context,props){

    val period = props["period"]!!.toInt()

    init {
        val tss = enableSeries(Interval.Min240,1000, false)

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
                longForMoneyIfFlat(1, 100_000)
                shortForMoneyIfFlat(0, 100_000)
            }else if(q < 0.05){
                longForMoneyIfFlat(0, 100_000)
                shortForMoneyIfFlat(1, 100_000)
            }else{
                flattenAll(0)
                flattenAll(1)
            }

            quantiles.add(spread)
        }

    }
}

fun spreadModel(): ModelConfig {
    val runConfig = ModelBacktestConfig().apply {
        startDate(LocalDate.now().minusDays(1500))
        interval = Interval.Min240
        backtestReaderFactory = DbReaderFactory(
            SourceName.MT5,
            Interval.Min240,
            roundedStartTime()
        )
        instruments = listOf("BNO", "USO")

    }
    return ModelConfig(SpreadModel::class, runConfig).apply{
        opt("period", 10,500, 5)
    }
}

fun main() {
    spreadModel().runStrat()
}