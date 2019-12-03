package firelib.common.core

import com.funstat.domain.InstrId
import com.funstat.store.MdStorageImpl
import com.funstat.tcs.GateEmulator
import com.funstat.tcs.SourceEmulator
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.instruments
import firelib.common.interval.Interval
import firelib.common.model.Model
import firelib.common.model.ModelContext
import firelib.common.model.buyIfNoPosition
import firelib.common.model.closePositionByTimeout
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.tradegate.TradeGate
import firelib.domain.Ohlc
import firelib.domain.ret
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue


object ProdRunner {

    fun runStrat(startTime: Instant,
                 executorService: ExecutorService,
                 cfg: ModelBacktestConfig,
                 realGate: TradeGate,
                 backtestMapper : (ticker : String)->InstrId,
                 realtimeMapper : (ticker : String)->InstrId,
                 realtimeSource : Source,
                 modelFactory: ModelFactory) {

        val storageImpl = MdStorageImpl()

        val endTime = cfg.instruments.map { it.ticker }.map ({
            storageImpl.updateMarketData(backtestMapper(it))
        }).min()!!


        val context = Launcher.makeContext(cfg, modelFactory, {})

        println("end of hist time is ${endTime}")


        val fut = executorService.submit(  Callable<Instant> {
            context.backtest(endTime)
        } )


        val pollers = cfg.instruments.map { realtimeMapper(it.ticker) }.map {

            println("initing realtime poller for  ${it}")

            val queue = LinkedBlockingQueue<Ohlc>()
            val ret = RealtimePoller(it!!, realtimeSource, endTime, cfg.rootInterval, queue)
            ret.thread.start()
            ret

        }

        context.tradeGate.setActiveReal(realGate)

        var ct = fut.get()

        println("backtest ended starting from ${ct}")

        while (true) {

            waitUntil(ct + Duration.ofMillis(500))

            executorService.submit({
                try {
                    pollers.forEachIndexed{ idx, poller ->
                        poller.queue.pollOhlcsTill(ct).forEach {
                            println("polled ohlc ${it.endTime}")
                            context.marketDataDistributor.addOhlc(idx, it)
                        }
                    }
                    context.time(ct)
                }catch (e : java.lang.Exception){
                    e.printStackTrace()
                }
            })

            ct += cfg.rootInterval.duration

        }
    }
}

fun main() {

    val executor = Executors.newSingleThreadExecutor({ Thread(it,"mainExecutor") })

    val gate = GateEmulator(executor)

    try {
        ProdRunner.runStrat(
                Instant.now(),
                executor,
                DummyModel.modelConfig(),
                gate,
                {InstrId(it,it,"na",it,SourceEmulator.SOURCE)},
                {InstrId(it,it,"na",it,SourceEmulator.SOURCE)},
                SourceEmulator(),
                DummyModel.modelFactory
        )

    }catch (e : Exception){
        e.printStackTrace()
    }
}

class DummyModel(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    init {
        orderManagers()[0].tradesTopic().subscribe {
            println(it)
        }
        val ts = context.mdDistributor.getOrCreateTs(0, Interval.Sec1, 100)
        ts.preRollSubscribe { ts->

            if(ts[0].ret() > 0){
                println("position ${orderManagers()[0].position()}")
                buyIfNoPosition(0,10)
            }


        }

        closePositionByTimeout(periods = 10, interval = Interval.Sec1)
    }

    companion object{
        val modelFactory : ModelFactory = { context, props ->
            DummyModel(context, props)
        }

        fun modelConfig (waitOnEnd : Boolean = false , divAdjusted: Boolean = false) : ModelBacktestConfig{
            return ModelBacktestConfig().apply {
                reportTargetPath = "/home/ivan/projects/chartpapa/market_research/dummy_model"
                instruments = instruments(listOf("sber"),
                        source = SourceEmulator.SOURCE,
                        divAdjusted = divAdjusted,
                        waitOnEnd = waitOnEnd)
                rootInterval = Interval.Sec1
                adjustSpread = makeSpreadAdjuster(0.0005)
            }
        }
    }
}

