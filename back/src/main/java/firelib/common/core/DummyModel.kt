package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.model.*
import firelib.common.ordermanager.sellAtMarket
import java.time.Instant
import java.time.LocalDate

class DummyModel(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    init {
        val ts = context.mdDistributor.getOrCreateTs(0, Interval.Sec10, 100)
        ts.preRollSubscribe { ts->
            if(Instant.now().toEpochMilli() - ts[0].endTime.toEpochMilli() < 100_000){
                println("ohlc ${ts[0]}" )
            }
            if(!orderManagers()[0].hasPendingState()){
                orderManagers()[0].sellAtMarket(1)
            }
            println("position ${orderManagers()[0].position()}")

        }

        closePositionByTimeout(periods = 2, interval = Interval.Sec10)
    }

    companion object{
        fun modelConfig () : ModelBacktestConfig {
            val cfg = ModelBacktestConfig(DummyModel::class).apply {
                instruments = listOf("SBER")
                startDate(LocalDate.now().minusDays(1))
                interval = Interval.Sec10
                adjustSpread = makeSpreadAdjuster(0.0005)
            }
            return cfg
        }
    }
}