package firelib.core.backtest

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.OptResourceParams
import firelib.core.backtest.opt.ParamsVariator
import firelib.core.misc.Batcher
import firelib.core.SimpleRunCtx
import firelib.core.report.ReportProcessor
import firelib.core.report.ReportWriter
import firelib.core.report.ReportWriter.clearReportDir
import firelib.core.report.ReportWriter.writeReport
import firelib.core.domain.Ohlc
import firelib.core.enableOhlcDumping
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Backtester{

    val log = LoggerFactory.getLogger(javaClass)

    init{
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            throwable.printStackTrace()
        }
    }

    fun runOptimized(cfg: ModelBacktestConfig) {
        log.info("Starting")

        val startTime = System.currentTimeMillis()

        val optCfg = cfg.optConfig
        val reportProcessor = ReportProcessor(optCfg.optimizedMetric,
                optCfg.params.map {it.name},
                minNumberOfTrades = optCfg.minNumberOfTrades)

        val variator = ParamsVariator(optCfg.params)

        val startDtGmt = cfg.interval.roundTime(cfg.startDateGmt)
        val endDtGmt = cfg.interval.roundTime(cfg.endDate)

        val endOfOptimize =  if(optCfg.optimizedPeriodDays < 0) endDtGmt.plusMillis(100)
        else startDtGmt.plus(optCfg.optimizedPeriodDays, ChronoUnit.DAYS)

        log.info("total number of models to test : ${variator.combinations()}")

        val optResourceParams: OptResourceParams = optCfg.resourceStrategy.getParams(variator.combinations())

        val executor = makeExecutor(optResourceParams)

        clearReportDir(cfg.reportTargetPath)

        var ohlcDumpSubscriptionNeeded = cfg.dumpOhlc

        var jobsList = emptyList<Batcher<Ohlc>>()

        sequence {
            yieldAll(variator)
        }.map {
            cfg.modelParams + it.mapValues { "${it.value}"}
        }.chunked(optResourceParams.batchSize).map { paramsVar ->
            val ctx = SimpleRunCtx(cfg)
            paramsVar.forEach { p ->
                ctx.addModel(p)
            }
            ctx
        }.forEach { ctx->
            if(ohlcDumpSubscriptionNeeded){
                jobsList = enableOhlcDumping(cfg, ctx.marketDataDistributor)
                ohlcDumpSubscriptionNeeded = false
            }
            executor.execute {
                ctx.backtest(endOfOptimize)
                reportProcessor.process(ctx.boundModels)
            }
            log.info("models scheduled for optimization ${ctx.boundModels.size}")
        }


        executor.shutdown()
        executor.awaitTermination(1,TimeUnit.DAYS)

        jobsList.forEach {it.cancelAndJoin()}

        require(reportProcessor.bestModels().isNotEmpty(), {"no models get produced!! probably because they did not generated enough trades"})

        log.info("Model optimized in ${(System.currentTimeMillis() - startTime) / 1000} sec")

        writeReport(reportProcessor.bestModels().last(), cfg)

        ReportWriter.writeOpt(cfg.getReportDbFile(), reportProcessor.estimates)

        log.info("Finished")
    }

    private fun makeExecutor(optResourceParams: OptResourceParams): ThreadPoolExecutor {
        val executor = (Executors.newFixedThreadPool(optResourceParams.threadCount) as ThreadPoolExecutor).apply {
            rejectedExecutionHandler = ThreadPoolExecutor.CallerRunsPolicy()
        }
        return executor
    }

    fun runSimple(cfg: ModelBacktestConfig) {
        clearReportDir(cfg.reportTargetPath)
        val ctx = SimpleRunCtx(cfg)
        ctx.addModel(cfg.modelParams)
        if (cfg.dumpOhlc) {
            enableOhlcDumping(
                config = cfg,
                marketDataDistributor = ctx.marketDataDistributor
            )
        }
        ctx.backtest(Instant.now())
        writeReport(ctx.boundModels.first(), cfg)
    }
}


