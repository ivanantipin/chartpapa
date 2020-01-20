package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.config.instruments
import firelib.common.interval.Interval
import firelib.common.model.*
import org.apache.commons.io.FileUtils
import java.time.Instant
import java.time.LocalDate

class DummyModel(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    init {
        val ts = context.mdDistributor.getOrCreateTs(0, Interval.Sec10, 100)
        ts.preRollSubscribe { ts->
            if(Instant.now().toEpochMilli() - ts[0].endTime.toEpochMilli() < 100_000){
                println("ohlc ${ts[0]}" )
            }
            if(orderManagers()[0].position() >= 0){
                shortForMoneyIfFlat(0, 10_000)
            }else{
                longForMoneyIfFlat(0,10_000)
            }
            println("position ${orderManagers()[0].position()}")

        }

        closePositionByTimeout(periods = 2, interval = Interval.Sec10)
    }

    companion object{
        val modelFactory : ModelFactory = { context, props ->
            DummyModel(context, props)
        }

        fun modelConfig (waitOnEnd : Boolean = false , divAdjusted: Boolean = false) : ModelBacktestConfig {

            val mapper = TcsTickerMapper()


            val cfg = ModelBacktestConfig(DummyModel::class).apply {
                instruments = instruments(listOf(mapper.map("sber")!!),
                        source = "TCS",
                        divAdjusted = divAdjusted,
                        waitOnEnd = waitOnEnd)
                startDate(LocalDate.now().minusDays(1))
                rootInterval = Interval.Sec10
                adjustSpread = makeSpreadAdjuster(0.0005)
            }

            FileUtils.forceDelete(cfg.getReportDbFile().toFile())

            return cfg
        }
    }
}