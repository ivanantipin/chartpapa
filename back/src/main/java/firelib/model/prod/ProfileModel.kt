package firelib.model.prod

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.core.report.dao.GeGeWriter
import firelib.indicators.MarketProfile
import firelib.indicators.defineLevels
import firelib.model.tickers
import java.time.LocalDate


data class ProfileEntry(
    val name: String,
    val price: Double,
    val value: Long,
    val entryPrice: Double,
    val levelFalse0: Double,
    val levelFalse1: Double,
    val levelTrue0: Double,
    val levelTrue1: Double,
    val prevPrice: Double,
    val incr: Double

)

class ProfileModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    val geGeWriter = GeGeWriter<ProfileEntry>(runConfig().getReportDbFile(), ProfileEntry::class)

    init {

        val window = props["window"]!!.toInt()
        val diff = props["diff"]!!.toInt()

        val series = enableSeries(Interval.Min10, interpolated = false, historyLen = window + 200)
        val daySeries = enableSeries(Interval.Day, interpolated = true, historyLen = 5)

        val profiles = instruments().map { MarketProfile() }
        val increms = DoubleArray(instruments().size, { Double.NaN })

        factorPoc(profiles, increms)

//        enableVolumeFactor()
        val barQuantFactor = factorBarQuantLow()

        factorAvgBarQuantLow(2)
        factorAvgBarQuantLow(3)
        factorAvgBarQuantLow(5)
//        enableMaDiffFactor(20)
//        enableMaDiffFactor(30)
//        enableMaDiffFactor(10)
//        enableMaDiffFactor(3)

        instruments().forEachIndexed({ idx, ticker ->
            val ts = series[idx]
            val dayts = daySeries[idx]
            val profile = profiles[idx]

            fun priceToLong(price: Double): Int {
                if (increms[idx].isNaN()) {
                    increms[idx] = price / 200.0;
                }
                return (price / increms[idx]).toInt()
            }

            var levelsFalse = emptyList<Int>()
            var levelsTrue = emptyList<Long>()

            ts.preRollSubscribe {
                val lprice = priceToLong(ts[0].close)
                profile.add(lprice, (ts[0].volume * ts[0].close).toLong())

                if (ts.count() > window) {
                    val ohlc = ts[window]
                    val rlprice = priceToLong(ohlc.close)
                    profile.reduceBy(rlprice, (ohlc.volume * ohlc.close).toLong())
                }

                if (currentTime().atMoscow().hour == 18 && currentTime().atMoscow().minute == 0) {
                    levelsFalse = profile.defineLevels(diff, false).map { it.first }
                }

                if (currentTime().atMoscow().hour == 18) {

                    val price0 = priceToLong(dayts[0].close)
                    val price1 = priceToLong(dayts[1].close)


//                    if (position(idx) > 0) {
//                        levelsTrue = profile.defineLevels(8, true).map { it.first }
//                        if (levelsTrue.any { it >= price1 && it < price0 }) {
//                            flattenAll(idx)
//                        }
//                    }

                    if (levelsFalse.any { it >= price1 && it < price0 } && barQuantFactor(idx) > 0.8) {
                        if (longForMoneyIfFlat(idx, 100_000)) {
                            val name = "${ticker}_${currentTime()}"

//                            geGeWriter.write(profile.priceToVol.map {
//                                ProfileEntry(name, it.key*priceIncr, it.value, dayts[0].close,
//                                    levelsFalse[0]*priceIncr,
//                                    levelsFalse[1]*priceIncr,
//                                    levelsTrue[0]*priceIncr,
//                                    levelsTrue[1]*priceIncr, price1*priceIncr, priceIncr)
//                            }.subList(1,profile.priceToVol.size))

                        }
                    }
                }
            }

        })

        closePosByCondition {
            !series[it][0].interpolated && positionDuration(it) > 3 * 24
        }
    }

    companion object {
        fun modelConfig(tradeSize : Int = 100_000): ModelConfig {
            return ModelConfig(ProfileModel::class, ModelBacktestConfig().apply {
                instruments = tickers
                startDate(LocalDate.now().minusDays(3000))
            }).apply {
                setTradeSize(tradeSize)
                param("window", 13000)
                param("diff", 18)
            }
        }

    }


}

fun main() {
    ProfileModel.modelConfig().runStrat()
}