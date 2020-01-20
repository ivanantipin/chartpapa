package firelib.common.core

import com.funstat.domain.InstrId
import com.funstat.store.MdStorageImpl
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.ordermanager.flattenAll
import firelib.common.report.ReportWriter
import firelib.common.tradegate.TradeGate
import firelib.domain.Ohlc
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue


object ProdRunner {

    fun runStrat(executorService: ExecutorService,
                 context: SimpleRunCtx,
                 realGate: TradeGate,
                 backtestMapper: (ticker: String) -> InstrId,
                 realtimeMapper: (ticker: String) -> InstrId,
                 realtimeSource: Source) {

        val cfg = context.modelConfig

        val endTime = updateMd(cfg, backtestMapper)

        val model = context.addModel(cfg.modelParams)

        val ioExecutor = Executors.newSingleThreadExecutor()

        val persistings = listOf(enableOrdersPersist(model, cfg.getReportDbFile(), ioExecutor, "orders_backtest"),
                enableTradeCasePersist(model, cfg.getReportDbFile(), ioExecutor, "trades_backtest"))


        println("end of hist time is ${endTime}")

        val fut = executorService.submit(Callable<Instant> {
            context.backtest(endTime)
        })

        var ct = fut.get()

        println("backtest ended starting from ${ct}")

        executorService.submit {
            model.orderManagers().forEach {it.flattenAll("switching gate")}
        }.get()


        persistings.forEach {it.cancel()}


        val queues = cfg.instruments.map { realtimeMapper(it.ticker) }.map {
            println("initing realtime poller for  ${it}")
            val queue = LinkedBlockingQueue<Ohlc>()
            realtimeSource.listen(it) { queue += it }
            queue
        }



        context.tradeGate.setActiveReal(realGate)


//        ioExecutor.submit {ReportWriter.writeReport(context.boundModels.first(),cfg)}.get()

        enableOrdersPersist(model, cfg.getReportDbFile(), ioExecutor)
        enableTradeCasePersist(model, cfg.getReportDbFile(), ioExecutor)
        enableTradeRtPersist(model, cfg.getReportDbFile(), ioExecutor)


        timeSequence(ct, cfg.rootInterval).forEach { ctt->
            executorService.submit {
                try {
                    queues.forEachIndexed { idx, poller ->
                        poller.pollOhlcsTill(ctt).forEach {
                            context.marketDataDistributor.addOhlc(idx, it)
                        }
                    }
                    context.time(ctt)
                } catch (e: java.lang.Exception) {
                    //fixme log
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateMd(cfg: ModelBacktestConfig, backtestMapper: (ticker: String) -> InstrId): Instant {
        val storageImpl = MdStorageImpl()
        return cfg.instruments.map { it.ticker }.map {
            storageImpl.updateMarketData(backtestMapper(it))
        }.min()!!
    }
}



fun timeSequence(startTime: Instant, interval: Interval, msShift : Long = 1000): Sequence<Instant> {
    var time = interval.roundTime(startTime)
    return sequence {
        while (true) {
            yield(time)
            time += interval.duration
            waitUntil(time.plusMillis(msShift)) // wait a bit for md to arrive
        }
    }
}

