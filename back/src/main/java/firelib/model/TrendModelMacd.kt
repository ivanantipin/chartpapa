package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.indicators.Ema
import java.time.LocalDate


class TrendModelMacd(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    val daytss = enableSeries(Interval.Day)


    init {
        val shortEmas = daytss.map { Ema(props["short"]!!.toInt(), it) }
        val longEmas = daytss.map { Ema(props["long"]!!.toInt(), it) }

        enableSeries(Interval.Min10)[0].preRollSubscribe {
            if (daytss[0].count() > 40 && currentTime().atMoscow().hour > 14 && !daytss[0][0].interpolated) {

                if (currentTime().atMoscow().minute == 40) {

                    val num = props["number"]!!.toInt()

                    val idxToRet = daytss.mapIndexed { idx, ts ->
                        val momentum = (shortEmas[idx].value() - longEmas[idx].value()) / ts[0].close
                        Pair(idx, momentum)
                    }

                    val indexed = idxToRet.filter { it.second.isFinite() && it.second > 0 }

                    val sortedBy = indexed.sortedBy { -it.second }

                    val sorted = sortedBy.subList(0, Math.min(num, indexed.size)).map { it.first }

                    idxToRet.forEach {
                        logRealtime { "return for ticker ${instruments()[it.first]} is ${it.second}" }
                    }

                    logRealtime { "=====" }

                    logRealtime { ("top is ${sorted.map { instruments()[it] }}") }


                    instruments().mapIndexed { idx, _ -> idx }.filter { !sorted.contains(it) }.forEach {
                        flattenAll(it)
                    }

                    sorted.forEach {
                        longForMoneyIfFlat(it, tradeSize())
                    }
                }
            }
        }
    }

    companion object {
        fun modelConfig(tradeSize: Int = 10_000): ModelConfig {
            return ModelConfig(TrendModelMacd::class).apply {
                setTradeSize(tradeSize)
                opt("short", 2, 10, 1)
                opt("long", 20, 60, 5)
                param("number", 5)
            }
        }
    }
}


fun main() {
    TrendModelMacd.modelConfig().runStrat(ModelBacktestConfig().apply {
        spreadAdjustKoeff = 0.0005
        instruments = tickers
        interval = Interval.Min10
        histSourceName = SourceName.FINAM
        startDate(LocalDate.now().minusDays(1500))
    })
}