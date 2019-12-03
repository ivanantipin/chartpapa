package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.config.OptResourceParams
import firelib.common.model.Model
import firelib.common.opt.ParamsVariator
import firelib.common.report.ReportProcessor
import firelib.common.report.ReportWriter
import firelib.common.report.ReportWriter.clearReportDir
import firelib.common.report.ReportWriter.writeReport
import firelib.common.timeboundscalc.BacktestPeriodCalc
import firelib.domain.Ohlc
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Launcher{

    init{
        Thread.setDefaultUncaughtExceptionHandler({thread, throwable ->
            throwable.printStackTrace()
        })
    }

    fun runOptimized(cfg: ModelBacktestConfig, factory : ModelFactory) {
        println("Starting")

        val startTime = System.currentTimeMillis()

        val optCfg = cfg.optConfig
        val reportProcessor = ReportProcessor(optCfg.optimizedMetric,
                optCfg.params.map {it.name},
                minNumberOfTrades = optCfg.minNumberOfTrades)

        val variator = ParamsVariator(optCfg.params)

        val (startDtGmt, endDtGmt) = BacktestPeriodCalc.calcBounds(cfg)

        val endOfOptimize =  if(optCfg.optimizedPeriodDays < 0) endDtGmt.plusMillis(100)
        else startDtGmt.plus(optCfg.optimizedPeriodDays, ChronoUnit.DAYS)

        println("total number of models to test : ${variator.combinations()}")

        val optResourceParams: OptResourceParams = optCfg.resourceStrategy.getParams(variator.combinations())

        val executor = makeExecutor(optResourceParams)

        clearReportDir(cfg.reportTargetPath)

        var ohlcDumpSubscriptionNeeded = cfg.dumpOhlc

        var jobsList = emptyList<Batcher<Ohlc>>()

        sequence({
            yieldAll(variator)
        }).map {
            cfg.modelParams + it.mapValues { "${it.value}"}
        }.chunked(optResourceParams.batchSize).map { paramsVar ->
            val ctx = SimpleRunCtx(cfg)
            paramsVar.forEach({ p ->
                ctx.addModel(factory, p)
            })
            ctx
        }.forEach { ctx->
            if(ohlcDumpSubscriptionNeeded){
                jobsList = enableOhlcDumping(cfg,ctx.marketDataDistributor)
                ohlcDumpSubscriptionNeeded = false
            }
            executor.execute {
                ctx.backtest(endOfOptimize)
                reportProcessor.process(ctx.boundModels)
            }
            println("models scheduled for optimization ${ctx.boundModels.size}")
        }


        executor.shutdown()
        executor.awaitTermination(1,TimeUnit.DAYS)

        jobsList.forEach {it.cancelAndJoin()}

        require(reportProcessor.bestModels().isNotEmpty(), {"no models get produced!! probably because they did not generated enough trades"})

        println("Model optimized in ${(System.currentTimeMillis() - startTime) / 1000} sec")

        writeReport(reportProcessor.bestModels().last(), cfg)

        ReportWriter.writeOpt(cfg.getReportDbFile(), reportProcessor.estimates)

        println("Finished")
    }

    private fun makeExecutor(optResourceParams: OptResourceParams): ThreadPoolExecutor {
        val executor = (Executors.newFixedThreadPool(optResourceParams.threadCount) as ThreadPoolExecutor).apply {
            rejectedExecutionHandler = ThreadPoolExecutor.CallerRunsPolicy()
        }
        return executor
    }

    fun runSimple(cfg: ModelBacktestConfig, fac: ModelFactory, ctxListener: (Model) -> Unit = {}) {

        val ctx = makeContext(cfg, fac, ctxListener)

        ReportWriter.copyPythonFiles(cfg)

        ctx.backtest(Instant.now())

        ctx.cancelBatchersAndWait()

        ReportWriter.writeStaticConf(cfg, ctx.boundModels[0])

        println("report written to ${cfg.reportTargetPath} you can run it , command 'jupyter lab'")

        println("done")
    }

    fun makeContext(cfg: ModelBacktestConfig, fac: ModelFactory, ctxListener: (Model) -> Unit): SimpleRunCtx {
        clearReportDir(cfg.reportTargetPath)

        val ioExecutor = Executors.newSingleThreadExecutor()

        val ctx = SimpleRunCtx(cfg)

        ctx.addModel(fac, cfg.modelParams)

        val model = ctx.boundModels[0].model
        ctx.batchers += enableOrdersPersist(model, cfg.getReportDbFile(), ioExecutor)
        ctx.batchers += enableTradeCasePersist(model, cfg.getReportDbFile(), ioExecutor)
        ctx.batchers += enableTradeRtPersist(model, cfg.getReportDbFile(), ioExecutor)
        ctxListener(model)
        if (cfg.dumpOhlc) {
            ctx.batchers += enableOhlcDumping(config = cfg, marketDataDistributor = ctx.marketDataDistributor)
        }
        return ctx
    }
}


