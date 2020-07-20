package firelib.model.prod

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.indicators.MarketProfile
import firelib.indicators.defineLevels
import firelib.model.tickers
import java.time.LocalDate


class ProfileModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val window = props["window"]!!.toInt()
        val diff = props["diff"]!!.toInt()

        val series = enableSeries(Interval.Min10, interpolated = false, historyLen = window + 200)
        val minSeries = enableSeries(Interval.Min1, interpolated = false, historyLen = window + 200)
        val daySeries = enableSeries(Interval.Day, interpolated = true, historyLen = 5)

        val profiles = instruments().map { MarketProfile() }
        val increms = DoubleArray(instruments().size, { Double.NaN })
        val barQuantFactor = factorBarQuantLow()


        instruments().forEachIndexed({ idx, ticker ->
            val ts = series[idx]
            val minSer = series[idx]
            val dayts = daySeries[idx]
            val profile = profiles[idx]

            fun priceToLong(price: Double): Int {
                if (increms[idx].isNaN()) {
                    increms[idx] = price / 200.0;
                }
                return (price / increms[idx]).toInt()
            }

            var levelsFalse = emptyList<Int>()

            minSer.preRollSubscribe {
                val lprice = priceToLong(ts[0].close)
                profile.add(lprice, (ts[0].volume * ts[0].close).toLong())

                if (ts.count() > window) {
                    val ohlc = ts[window]
                    val rlprice = priceToLong(ohlc.close)
                    profile.reduceBy(rlprice, (ohlc.volume * ohlc.close).toLong())
                }
            }

            ts.preRollSubscribe {

                if (currentTime().atMoscow().hour == 18 && currentTime().atMoscow().minute == 0) {
                    levelsFalse = profile.defineLevels(diff, false).map { it.first }
                }

                if (currentTime().atMoscow().hour == 18) {
                    val price0 = priceToLong(dayts[0].close)
                    val price1 = priceToLong(dayts[1].close)
                    if (levelsFalse.any { it >= price1 && it < price0 } && barQuantFactor(idx) > 0.8) {
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
        fun modelConfig(tradeSize : Int = 100_000): ModelConfig {
            return ModelConfig(ProfileModel::class, ModelBacktestConfig().apply {
                instruments = tickers
                interval = Interval.Min1
                histSourceName = SourceName.FINAM
                startDate(LocalDate.now().minusDays(600))
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