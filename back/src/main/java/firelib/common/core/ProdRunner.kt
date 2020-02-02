package firelib.common.core

import firelib.store.MdStorageImpl
import firelib.common.core.config.ModelBacktestConfig
import firelib.common.core.report.ReportWriter
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


object ProdRunner {

    fun runStrat(executorService: ExecutorService,
                 context: SimpleRunCtx,
                 realGate: TradeGate,
                 realReaderFactory: ReaderFactory) {

        val cfg = context.modelConfig

        val model = context.addModel(cfg.modelParams)

        val ioExecutor = Executors.newSingleThreadExecutor()

        val endTime = updateMd(cfg)

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


        persistings.forEach {it.cancelAndJoin()}


        context.tradeGate.setActiveReal(realGate)


        ioExecutor.submit { ReportWriter.writeReport(context.boundModels.first(),cfg)}.get()

        val realReaders = cfg.instruments.map {
            realReaderFactory.makeReader(it)
        }

        enableOrdersPersist(model, cfg.getReportDbFile(), ioExecutor)
        enableTradeCasePersist(model, cfg.getReportDbFile(), ioExecutor)
        enableTradeRtPersist(model, cfg.getReportDbFile(), ioExecutor)

        timeSequence(cfg.interval.roundTime(Instant.now()), cfg.interval).forEach {
            executorService.submit {
                context.progress(it, realReaders)
            }.get()
        }
    }

    fun updateMd(cfg: ModelBacktestConfig): Instant {
        val storageImpl = MdStorageImpl()
        return cfg.instruments.map(cfg.backtestHistSource::mapSecurity).map {
            storageImpl.updateMd(it, cfg.backtestHistSource)
        }.min()!!
    }
}

