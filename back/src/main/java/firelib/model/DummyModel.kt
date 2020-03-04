package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.domain.Interval
import firelib.core.makePositionEqualsTo
import java.time.Instant
import java.time.LocalDate

class DummyModel(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    init {
        val ts = context.mdDistributor.getOrCreateTs(0, Interval.Min1, 100)
        ts.preRollSubscribe { ts->
            if(Instant.now().toEpochMilli() - ts[0].endTime.toEpochMilli() < 100_000){
                log.info("ohlc ${ts[0]}" )
            }
            if(!ts[0].interpolated){
                if(orderManagers()[0].position() <= 0 && !orderManagers()[0].hasPendingState()){
                    orderManagers()[0].makePositionEqualsTo(1)
                } else if(orderManagers()[0].position() > 0 && !orderManagers()[0].hasPendingState()){
                    orderManagers()[0].makePositionEqualsTo(0)
                }
            }
            log.info("position ${orderManagers()[0].position()} time is ${ts[0].endTime}")

        }

        closePositionByTimeout(periods = 2, interval = Interval.Min10)
    }

    companion object{
        fun modelConfig () : ModelBacktestConfig {
            val cfg = ModelBacktestConfig(DummyModel::class).apply {
                instruments = listOf("SBER")
                startDate(LocalDate.now().minusDays(1))
                interval = Interval.Min1
                adjustSpread = makeSpreadAdjuster(0.0005)
            }
            return cfg
        }
    }
}