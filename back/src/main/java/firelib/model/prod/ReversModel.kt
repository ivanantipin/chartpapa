package firelib.model.prod

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.domain.isUpBar
import firelib.core.misc.atMoscow
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.SignalType
import firelib.model.*
import java.time.LocalDate


class ReversModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        val ts4h = enableSeries(Interval.Min240, interpolated = false)


        factorVolume()
        factorBarQuantLow()
        factorMaDiff(20)
        factorBarQuant()
        factorWeekday()
        factorHour()

        ts4h.forEachIndexed { idx, hts ->
            val sequenta = Sequenta()
            var setup: Sequenta.Setup? = null

            hts.preRollSubscribe { ts ->
                if (!ts[0].interpolated) {
                    logRealtime { "day roll ${instruments()[idx]} -  ${ts[0]}" }
                    val siggis = sequenta.onOhlc(ts[0])

                    siggis.filter { it.type == SignalType.SetupReach }
                        .forEach {
                            logRealtime { "established setup for tdst ${it.reference.tdst}" }
                            if(it.reference.isDown){
                                setup = it.reference
                            }
                        }

                    if (setup != null && setup!!.isDown) {
                        if (ts[0].close > ts[1].close &&
                            ts[1].isUpBar() &&
                            ts[1].close > setup!!.tdst &&
                            ts[2].close < setup!!.tdst
                        ) {
                            longForMoneyIfFlat(idx, tradeSize())
                            setup = null;
                        }
                    }

                    siggis.filter { it.type == SignalType.SetupReach }
                        .forEach {
                            if (position(idx) > 0 && it.reference.isDown) {
                                logRealtime { "flattening because of counter setup developed on 4h" }
                                flattenAll(idx)
                            }
                        }

                    siggis.filter { it.type == SignalType.Signal && it.reference.recycleRef == null && it.reference.isUp }
                        .forEach {
                            if (position(idx) > 0) {
                                logRealtime { "flattening because of signal non recycled" }
                                flattenAll(idx)
                            }
                        }

                }
            }
        }
    }

    companion object {
        fun modelConfig(tradeSize: Int = 10_000): ModelConfig {
            return ModelConfig(ReversModel::class, ModelBacktestConfig().apply {
                instruments = tickers
                startDate(LocalDate.now().minusDays(5000))
            }).apply {
                setTradeSize(tradeSize)
            }
        }
    }
}


fun main() {
    ReversModel.modelConfig().runStrat()
}