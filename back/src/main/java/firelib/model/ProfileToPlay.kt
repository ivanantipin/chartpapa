package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.indicators.MarketProfile
import firelib.indicators.defineLevels
import java.time.LocalDate



class ProfileToPlay(context: ModelContext, val props: Map<String, String>) : Model(context, props) {
    init {

        val window = props["window"]!!.toInt()
        val diff = props["diff"]!!.toInt()

        val series = enableSeries(Interval.Min15, interpolated = false, historyLen = window + 200)
        val daySeries = enableSeries(Interval.Day, interpolated = true, historyLen = 5)

        val profiles = instruments().map { MarketProfile() }
        val increms = DoubleArray(instruments().size, { Double.NaN })

        instruments().forEachIndexed({ idx, ticker ->
            val ts = series[idx]
            val dayts = daySeries[idx]
            val profile = profiles[idx]

            fun priceToLong(price: Double): Int {
                if (increms[idx].isNaN()) {
                    increms[idx] = price / 200.0;
                    println("incr = ${increms[idx]}")
                }
                return (price / increms[idx]).toInt()
            }

            var levelsFalse = emptyList<Int>()

            ts.preRollSubscribe {

                val lprice = priceToLong(ts[0].close)
                profile.add(lprice, (ts[0].volume * ts[0].close).toLong())

                if (ts.count() > window) {
                    val ohlc = ts[window]
                    val rlprice = priceToLong(ohlc.close)
                    profile.reduceBy(rlprice, (ohlc.volume * ohlc.close).toLong())
                }

                if (currentTime().atMoscow().hour == 18 && currentTime().atMoscow().minute == 30) {
                    levelsFalse = profile.defineLevels(diff, false).map { it.first }
                }

                if (currentTime().atMoscow().hour == 18 && currentTime().atMoscow().minute == 30) {

                    val price0 = priceToLong(dayts[0].close)
                    val price1 = priceToLong(dayts[1].close)


                    if (levelsFalse.any { it >= price1 && it < price0 } ) {
                        longForMoneyIfFlat(idx, 100_000)
                    }
                }
            }

        })

        closePosByCondition {
            !series[it][0].interpolated && positionDuration(it) > 3 * 24
        }
    }

    companion object {
        //MdStorageImpl().updateMarketData(InstrId(code = "ALLFUTSi", source = SourceName.MT5.name), interval = Interval.Min15);
        fun modelConfig(tradeSize : Int = 100_000): ModelConfig {
            return ModelConfig(ProfileToPlay::class, ModelBacktestConfig().apply {
                instruments = listOf("ALLFUTRTSI")
                interval= Interval.Min15
                startDate(LocalDate.now().minusDays(5000))
                histSourceName = SourceName.MT5
            }).apply {
                setTradeSize(tradeSize)
                param("window", 1300)
                param("diff", 4)
            }
        }

    }


}

fun main() {
    ProfileToPlay.modelConfig().runStrat()
}