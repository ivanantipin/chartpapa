package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.ret
import firelib.core.misc.Quantiles
import firelib.core.misc.atNy
import firelib.core.timeseries.TimeSeries
import firelib.indicators.ATR
import firelib.model.prod.*
import java.time.LocalDate
import java.time.LocalTime

class VXX_BuyStrat(context: ModelContext, val props: Map<String, String>) : Model(context, props) {
    init {

        val min5 = enableSeries(Interval.Min5, 100)
        val series = min5[0]
        val daySer = enableSeries(Interval.Day, 100)[0]

        factorHourUs()
        factorWeekday()
        val dayFunc = factorDay()

        closePosByCondition {
            currentTime().atNy().toLocalTime() >= LocalTime.of(15,0)
        }

        enableFactor("contango", {idx->
            contCalc(min5)
        } )

        var cont = 0.0
        var pp = 0.0

        val calcContDiff: (Int) -> Double = {
            contCalc(min5) - cont
        }
        enableFactor("contDiff", calcContDiff)

        enableFactor("gap",{
            val ret = (daySer[0].close - pp) / pp
            if(ret.isFinite() && ret < 1) ret else -1.0
        })

        series.preRollSubscribe {
            if(!daySer[0].interpolated){
                val locTime = currentTime().atNy().toLocalTime()

                if(locTime == LocalTime.of(15, 45)){
                    cont = contCalc(min5)
                    pp = daySer[0].close
                }

                if(locTime == LocalTime.of(9, 35)){
                    shortForMoneyIfFlat(0,100_000)
                }
            }
        }
    }

    private fun contCalc(min5: List<TimeSeries<Ohlc>>): Double {
        val futIdx = min5[1][0].close
        val vix = min5[2][0].close
        return (futIdx - vix) / futIdx
    }
}

fun vxxBuyModel(): ModelConfig {
    return ModelConfig(VXX_BuyStrat::class)
}

fun main() {
    vxxBuyModel().runStrat(ModelBacktestConfig().apply {
        interval = Interval.Min1
        histSourceName = SourceName.IQFEED
        market = "NA"
        instruments = listOf("UVXY", "@VX#", "VIX.XO")
        maxRiskMoneyPerSec = 100_000
        startDate(LocalDate.now().minusDays(5000))
        //endDate(LocalDate.now().minusDays(300))
    })
}