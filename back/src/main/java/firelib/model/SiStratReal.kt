package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.ret
import firelib.core.misc.Quantiles
import firelib.core.misc.atMoscow
import firelib.core.timeseries.ret
import firelib.indicators.Ma
import firelib.indicators.SimpleMovingAverage
import java.time.LocalDate


class SiStratReal(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val intraTs = enableSeries(Interval.Min60, 100, false)[0]
        val dayTs = enableSeries(Interval.Day, interpolated = true)[0]

        val longQQ = Quantiles<Any>(100)

        val period = props["period"]!!.toInt()

        val ma = SimpleMovingAverage(20,false)

        intraTs.preRollSubscribe {
            val mm = it[0].close - ma.value()
            val qq = longQQ.getQuantile(mm)
            if(qq < 0.1){
                shortForMoneyIfFlat(0,100_000)
            }
        }

        dayTs.preRollSubscribe {
            if(!it[0].interpolated){
                longQQ.add(it[0].close - ma.value())
                ma.add(it[0].close)
            }
        }

        closePosByCondition { idx->
            positionDuration(0) > 24 && position(idx) != 0
        }

    }
}

fun siReal(): ModelBacktestConfig {
    return ModelBacktestConfig(SiStratReal::class).apply {
        instruments = listOf("SPFB_Si")
        opt("period", 2, 30, 2)
        dumpInterval = Interval.Min30
        startDate(LocalDate.now().minusDays(1500))
    }
}

fun main() {
    siReal().runStrat()
}