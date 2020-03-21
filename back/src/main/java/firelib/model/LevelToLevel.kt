package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.misc.Quantiles
import firelib.core.report.dao.GeGeWriter
import firelib.core.timeseries.TimeSeries
import firelib.core.domain.Ohlc
import firelib.core.misc.UtilsHandy
import firelib.core.misc.atMoscow
import firelib.core.misc.toInstantDefault
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.SignalType
import java.nio.file.Paths
import java.time.LocalDate
import java.util.*

data class Level(
    val value: Double
)

class LevelToLevel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        val daytss = enableSeries(Interval.Day, interpolated = true)

        val hourTs = enableSeries(Interval.Min10, interpolated = false)





        daytss.forEachIndexed { idx, dayts ->
            val levels = LinkedList<Double>()
            val sorted = LinkedList<Double>()

            var target = Double.MAX_VALUE
            var stop = Double.MIN_VALUE


            val sequenta = Sequenta()
            dayts.preRollSubscribe { ts ->
                sequenta.onOhlc(ts[0])
                    .filter { it.type == SignalType.SetupReach  && !it.reference.up}
                    .forEach {
                        levels += it.reference.tdst
                        sorted += it.reference.tdst
                        sorted.sort()
                        if (levels.size > 1) {
                            val l = levels.removeFirst()
                            sorted.remove(l)
                        }
                    }
            }


            hourTs[idx].preRollSubscribe {ts->

                if(!ts[0].interpolated && currentTime().atMoscow().hour == 18){

                    if(levels.isNotEmpty()){
                        val lvl = levels.first

                        if(dayts[1].close < lvl && ts[0].close > lvl){
                            longForMoneyIfFlat(idx, 100_000)
                        }
                    }
                }
            }

            closePosByCondition {
                it == idx && !hourTs[it][0].interpolated &&
                        positionDuration(it) > 72
            }

        }

    }

}

fun main() {
//    UtilsHandy.updateRussianDivStocks()
    val conf = ModelBacktestConfig(LevelToLevel::class).apply {
        instruments = tickers
        param("levels",17)
        startDate(LocalDate.now().minusDays(3000))
    }
    conf.runStrat()
}
