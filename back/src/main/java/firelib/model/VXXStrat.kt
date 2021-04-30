package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.atNy
import firelib.core.timeseries.TimeSeries
import firelib.indicators.ATR
import firelib.model.prod.factorDay
import java.time.LocalDate
import java.time.LocalTime

class VXXStrat(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    private fun contCalc(min5: List<TimeSeries<Ohlc>>): Double {
        val futIdx = min5[1][0].close
        val vix = min5[2][0].close
        return (futIdx - vix) / futIdx
    }


    init {

        val min5 = enableSeries(Interval.Min5, 100)
        val series = min5[0]
        val daySer = enableSeries(Interval.Day, 100)[0]

        val atr = ATR(26, enableSeries(Interval.Min15, 100, false)[0])

        closePosByCondition {
            currentTime().atNy().toLocalTime() >= LocalTime.of(15,45)
        }

        factorDay()

        enableFactor("contango", {idx->
            contCalc(min5)
        } )

        series.preRollSubscribe {

            if(!daySer[0].interpolated){

                val locTime = currentTime().atNy().toLocalTime()
                val thr = daySer[1].close - atr.value()*5.5
                if(locTime > LocalTime.of(9, 45) && locTime < LocalTime.of(14,0)){
                    if(daySer[0].open > thr && daySer[0].close < thr){
                        shortForMoneyIfFlat(0, 100_000)
                    }
                }

                if(daySer[0].close > thr + atr.value()*2){
                    flattenAll(0)
                }

            }
        }
    }
}

fun vxxModel(): ModelConfig {
    return ModelConfig(VXXStrat::class)
}

fun main() {
    vxxModel().runStrat(ModelBacktestConfig().apply {
        interval = Interval.Min1
        histSourceName = SourceName.IQFEED
        market = "NA"
        instruments = listOf("UVXY", "@VX#", "VIX.XO")
        maxRiskMoneyPerSec = 100_000
        startDate(LocalDate.now().minusDays(6000))
        //endDate(LocalDate.now().minusDays(300))
    })
}