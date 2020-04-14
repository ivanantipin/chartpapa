package firelib.model.prod

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.SignalType
import firelib.model.*
import java.time.LocalDate


class ReverseModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        val daytss = enableSeries(Interval.Day, interpolated = true)
        val hh = enableSeries(Interval.Min240, interpolated = false)
        val hourTs = enableSeries(Interval.Min10, interpolated = false)

        hh.forEachIndexed { idx, hts ->
            val sequenta = Sequenta()
            hts.preRollSubscribe { ts ->
                if (!ts[0].interpolated) {
                    val siggis = sequenta.onOhlc(ts[0])
                    siggis
                        .filter { it.type == SignalType.SetupReach }
                        .forEach {
                            if (position(idx) > 0 && it.reference.isDown) {
                                logRealtime { "flattening because of counter setup developed on 4h"}
                                flattenAll(idx)
                            }
                        }
                    siggis.filter { it.type == SignalType.Signal && it.reference.recycleRef == null && it.reference.isUp }
                        .forEach {
                            if(position(idx) > 0){
                                logRealtime { "flattening because of signal non recycled" }
                                flattenAll(idx)
                            }
                        }
                }
            }
        }

        daytss.forEachIndexed { idx, dayts ->
            var setup: Sequenta.Setup? = null
            val sequenta = Sequenta()
            dayts.preRollSubscribe { ts ->
                if (!ts[0].interpolated) {
                    logRealtime { "day roll ${instruments()[idx]} -  ${ts[0]}" }
                    sequenta.onOhlc(ts[0])
                        .filter { it.type == SignalType.SetupReach }
                        .forEach {
                            logRealtime { "established setup for tdst ${it.reference.tdst}" }
                            setup = it.reference
                        }
                }
            }

            hourTs[idx].preRollSubscribe { ts ->
                if (!ts[0].interpolated && currentTime().atMoscow().hour == 18) {

                    logRealtime { "checking setup ${instruments()[idx]} -  ${ts[0]}" }

                    if (setup != null && setup!!.isUp) {
                        if (dayts[0].close < dayts[1].close &&
                            dayts[0].close < setup!!.tdst &&
                            dayts[2].close > setup!!.tdst
                        ) {
                            if(position(idx) > 0){
                                logRealtime { "flattening because break of tdst ${setup!!.tdst}" }
                                flattenAll(idx)
                            }

                            setup = null;
                            //shortForMoneyIfFlat(idx, 100_000)
                        }
                    }
                    if (setup != null && setup!!.isDown) {
                        if (dayts[0].close > dayts[1].close &&
                            dayts[0].close > setup!!.tdst &&
                            dayts[2].close < setup!!.tdst
                        ) {
                            longForMoneyIfFlat(idx, tradeSize())
                            setup = null;
                        }
                    }
                }
            }
        }
    }

    companion object{
        fun modelConfig(tradeSize : Int = 10_000) : ModelConfig{
            return ModelConfig(ReverseModel::class, ModelBacktestConfig().apply {
                instruments = tickers.filter { it != "irao" }
                startDate(LocalDate.now().minusDays(3000))
            }).apply {
                setTradeSize(tradeSize)
            }
        }
    }
}


fun main() {
    ReverseModel.modelConfig().runStrat()
}