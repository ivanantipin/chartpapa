package firelib.core

import firelib.core.store.MdStorageImpl
import firelib.core.config.ModelBacktestConfig
import firelib.core.misc.timeSequence
import firelib.core.report.OmPosition
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

        val endTime = updateMd(cfg, false)

        val persistings = listOf(enableOrdersPersist(model, cfg.getReportDbFile(), ioExecutor),
            enableTradeCasePersist(model, cfg.getReportDbFile(), ioExecutor))


        log.info("end of hist time is ${endTime}")

        val fut = executorService.submit(Callable<Instant> {
            context.backtest(cfg.interval.roundTime(Instant.now()))
        })

        val curentPoses = readCurrentPositions(cfg.getProdDbFile())

        var nextTimeToProgress = fut.get()

        log.info("backtest ended starting from ${nextTimeToProgress}")

        executorService.submit {
            model.orderManagers().forEach {it.flattenAll("switching gate")}
        }.get()


        persistings.forEach {it.cancelAndJoin()}

        executorService.submit {
            model.orderManagers().forEach {
                val pos = curentPoses.getOrDefault(it.security().toUpperCase(), OmPosition(0,0))
                log.info("restored position for ${it.security()} to ${pos}")
                it.updatePosition(pos.position, Instant.ofEpochMilli(pos.posTime))
            }
        }.get()


        context.tradeGate.setActiveReal(realGate)


        ioExecutor.submit {
            ReportWriter.clearReportDir(cfg.reportTargetPath)
            ReportWriter.writeReport(context.boundModels.first(),cfg)
        }.get()

        val realReaders = cfg.instruments.map {
            realReaderFactory.makeReader(it)
        }

        enableOrdersPersist(model, cfg.getProdDbFile(), ioExecutor)
        enableTradeCasePersist(model, cfg.getProdDbFile(), ioExecutor)
        enableTradeRtPersist(model, cfg.getProdDbFile(), ioExecutor)

        timeSequence(nextTimeToProgress, cfg.interval).forEach {
            try{
                executorService.submit {
                    context.progress(it, realReaders)
                }.get()
            }catch (e : Exception){
                log.error("error iterating loop for timestamp ${it}", e)
            }

        }
    }

    fun updateMd(cfg: ModelBacktestConfig, useMin : Boolean): Instant {
        val storageImpl = MdStorageImpl()
        val updated = cfg.instruments.map(cfg.backtestHistSource::mapSecurity).associateBy ({},
            {            storageImpl.updateMd(it, cfg.backtestHistSource)})

        println("updated data to ${updated}")
        return if(useMin) updated.values!!.min()!! else updated.values!!.max()!!
    }
}

