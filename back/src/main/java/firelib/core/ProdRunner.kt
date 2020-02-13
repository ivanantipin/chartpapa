package firelib.core

import firelib.core.store.MdStorageImpl
import firelib.core.config.ModelBacktestConfig
import firelib.core.misc.timeSequence
import firelib.core.report.ReportWriter
import firelib.core.report.Sqls.readCurrentPositions
import firelib.core.store.ReaderFactory
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


object ProdRunner {

    fun runStrat(executorService: ExecutorService,
                 context: SimpleRunCtx,
                 realGate: TradeGate,
                 realReaderFactory: ReaderFactory
    ) {

        val log = LoggerFactory.getLogger(javaClass)

        val cfg = context.modelConfig

        val model = context.addModel(cfg.modelParams)

        val ioExecutor = Executors.newSingleThreadExecutor()

        val endTime = updateMd(cfg)

        val persistings = listOf(enableOrdersPersist(model, cfg.getReportDbFile(), ioExecutor, "orders_backtest"),
            enableTradeCasePersist(model, cfg.getReportDbFile(), ioExecutor, "trades_backtest"))


        log.info("end of hist time is ${endTime}")

        val fut = executorService.submit(Callable<Instant> {
            context.backtest(endTime)
        })

        val curentPoses = readCurrentPositions(cfg.getReportDbFile())

        var ct = fut.get()

        log.info("backtest ended starting from ${ct}")

        executorService.submit {
            model.orderManagers().forEach {it.flattenAll("switching gate")}
        }.get()


        persistings.forEach {it.cancelAndJoin()}

        executorService.submit {
            model.orderManagers().forEach {
                val pos = curentPoses.getOrDefault(it.security(), 0)
                log.info("restored position for ${it.security()} to ${pos}")
                it.updatePosition(pos)
            }
        }.get()


        context.tradeGate.setActiveReal(realGate)


        ioExecutor.submit { ReportWriter.writeReport(context.boundModels.first(),cfg)}.get()

        val realReaders = cfg.instruments.map {
            realReaderFactory.makeReader(it)
        }

        enableOrdersPersist(model, cfg.getReportDbFile(), ioExecutor)
        enableTradeCasePersist(model, cfg.getReportDbFile(), ioExecutor)
        enableTradeRtPersist(model, cfg.getReportDbFile(), ioExecutor)

        timeSequence(cfg.interval.roundTime(Instant.now()), cfg.interval).forEach {
            try{
                executorService.submit {
                    context.progress(it, realReaders)
                }.get()
            }catch (e : Exception){
                log.error("error iterating loop for timestamp ${it}")
            }

        }
    }

    fun updateMd(cfg: ModelBacktestConfig): Instant {
        val storageImpl = MdStorageImpl()
        return cfg.instruments.map(cfg.backtestHistSource::mapSecurity).map {
            storageImpl.updateMd(it, cfg.backtestHistSource)
        }.min()!!
    }
}

