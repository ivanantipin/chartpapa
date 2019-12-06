package firelib.common.core

import com.funstat.tcs.TcsGate
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.instruments
import firelib.common.interval.Interval
import firelib.common.model.*
import ru.tinkoff.invest.openapi.data.Currency
import ru.tinkoff.invest.openapi.wrapper.SandboxContext
import java.time.Instant
import java.util.concurrent.Executors

class DummyModel(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    init {
        val ts = context.mdDistributor.getOrCreateTs(0, Interval.Min1, 100)
        ts.preRollSubscribe { ts->
            if(Instant.now().toEpochMilli() - ts[0].endTime.toEpochMilli() < 100_000){
                println("ohlc ${ts[0]}" )
            }
            buyViaLimitIfNoPosition(0,10_000)
        }

        closePositionByTimeout(periods = 2, interval = Interval.Min1)
    }

    companion object{
        val modelFactory : ModelFactory = { context, props ->
            DummyModel(context, props)
        }

        fun modelConfig (waitOnEnd : Boolean = false , divAdjusted: Boolean = false) : ModelBacktestConfig {
            return ModelBacktestConfig().apply {
                reportTargetPath = "/home/ivan/projects/chartpapa/market_research/dummy_model"
                instruments = instruments(listOf("sber"),
                        source = "TCS",
                        divAdjusted = divAdjusted,
                        waitOnEnd = waitOnEnd)
                rootInterval = Interval.Min1
                adjustSpread = makeSpreadAdjuster(0.0005)
            }
        }
    }
}

fun main() {

    val executor = Executors.newSingleThreadExecutor({ Thread(it,"mainExecutor") })

    val mapper = TcsTickerMapper()

    (mapper.context as SandboxContext).setCurrencyBalance(Currency.RUB, 1000_000.toBigDecimal()).get()

    val gate = TcsGate(executor, mapper)

    try {
        ProdRunner.runStrat(
                executor,
                DummyModel.modelConfig(),
                gate,
                {mapper.map(it)!!},
                {mapper.map(it)!!},
                mapper.source,
                DummyModel.modelFactory
        )

    }catch (e : Exception){
        e.printStackTrace()
    }
}
