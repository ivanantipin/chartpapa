package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.range
import firelib.core.domain.ret
import firelib.core.flattenAll
import firelib.core.misc.Quantiles
import firelib.core.misc.atMoscow
import firelib.core.store.DbMapper
import firelib.core.store.MdStorageImpl
import firelib.core.store.finamMapperWriter
import firelib.core.store.populateMapping
import firelib.finam.FinamDownloader
import firelib.indicators.Ma
import firelib.indicators.MarketProfile
import java.lang.Double.min
import java.time.DayOfWeek
import java.time.LocalDate


class ProfileModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {


    init {

        val priceIncr = instruments().map { Double.NaN }.toDoubleArray()

        fun priceToLong(idx : Int, price : Double) : Long{
            if(priceIncr[idx].isNaN()){
                priceIncr[idx] = price / 100.0;
            }
            return (price / priceIncr[idx]).toLong()
        }

        val profiles = instruments().map { MarketProfile() }

        val series = enableSeries(Interval.Min10, interpolated = false, historyLen = 1300)

        val daySeries = enableSeries(Interval.Day, interpolated = true, historyLen = 5)

        series.forEachIndexed { idx, ts->
            ts.preRollSubscribe {
                val lprice = priceToLong  (idx, ts[0].close)
                profiles[idx].add(lprice, (ts[0].volume * ts[0].close).toLong())

                if (ts.count() > 1250) {
                    val ohlc = ts[1200]
                    val rlprice = priceToLong(idx, ohlc.close)
                    profiles[idx].reduceBy(rlprice, (ohlc.volume * ohlc.close).toLong())
                }
            }
        }

        val mas = daySeries.map {
            Ma(30, it)
        }

        val maQuantiles = quantiles(1000)


        enableFactor("ma30") {
            val ret = maQuantiles[it].getQuantile(daySeries[it][0].close - mas[it].value())
            if(ret.isNaN()) 0.5 else ret
        }

        daySeries.forEachIndexed{idx, ts->
            ts.preRollSubscribe {
                if(it.count() > 30){
                    maQuantiles[idx].add(daySeries[idx][0].close - mas[idx].value())
                }
            }
        }

        prerollSubscribe(Interval.Min30) { time, md ->
            profiles.forEachIndexed { idx, prof ->
                if(!series[idx][0].interpolated && series[idx].count() > 1200){
                    val range = prof.calcVaRange()
                    if ((prof.pocPrice - range.first).toDouble() / (range.second - range.first).toDouble() < 0.1) {
                        longForMoneyIfFlat(idx, 100_000)
                    }
                }
            }
        }

        closePosByCondition {
            !series[it][0].interpolated && positionDuration(it) > 3*24
        }
    }



}

fun profileModelConfig(): ModelBacktestConfig {
    return ModelBacktestConfig(ProfileModel::class).apply {
        instruments = tickers
//        tickerToDiv = DivHelper.getDivs()
        startDate(LocalDate.now().minusDays(3000))
        param("period", 33)
        param("number", 5)
    }
}

fun main() {
    val conf = profileModelConfig()
    conf.runStrat()
}
