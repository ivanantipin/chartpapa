package firelib.core.backtest

import firelib.core.SimpleRunCtx
import firelib.core.backtest.opt.ParamsVariator
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.OptResourceParams
import firelib.core.domain.Interval
import firelib.core.domain.ModelOutput
import firelib.core.domain.Ohlc
import firelib.core.enableOhlcDumping
import firelib.core.misc.Batcher
import firelib.core.report.ReportProcessor
import firelib.core.report.ReportWriter
import firelib.core.report.ReportWriter.clearReportDir
import firelib.core.report.ReportWriter.writeReport
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.*

object Backtester {

    val log = LoggerFactory.getLogger(javaClass)


    val ioExecutor = Executors.newSingleThreadExecutor { Thread(it).apply { isDaemon = true } }

    init {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            throwable.printStackTrace()
        }
    }

    fun runOptimized(mc: ModelConfig, runConfig: ModelBacktestConfig) {
        log.info("Starting")

        val cfg = runConfig

        val startTime = System.currentTimeMillis()

        val optCfg = mc.optConfig
        val reportProcessor = ReportProcessor(
            optCfg.optimizedMetric,
            optCfg.params.map { it.name },
            minNumberOfTrades = optCfg.minNumberOfTrades
        )

        val variator = ParamsVariator(optCfg.params)

        val startDtGmt = cfg.interval.roundTime(cfg.startDateGmt)
        val endDtGmt = cfg.interval.roundTime(cfg.endDate)

        val endOfOptimize = if (optCfg.optimizedPeriodDays < 0) endDtGmt.plusMillis(100)
        else startDtGmt.plus(optCfg.optimizedPeriodDays, ChronoUnit.DAYS)

        log.info("total number of models to test : ${variator.combinations()}")

        val optResourceParams: OptResourceParams = optCfg.resourceStrategy.getParams(variator.combinations())

        val executor = makeExecutor(optResourceParams)

        clearReportDir(cfg.reportTargetPath)

        var ohlcDumpSubscriptionNeeded = cfg.dumpInterval != Interval.None

        var jobsList = emptyList<Batcher<Ohlc>>()

        sequence {
            yieldAll(variator)
        }.map {
            mc.modelParams + it.mapValues { "${it.value}" }
        }.chunked(optResourceParams.batchSize).map { paramsVar ->
            val ctx = SimpleRunCtx(cfg)
            paramsVar.forEach { p ->
                ctx.addModel(p, mc)
            }
            ctx
        }.forEach { ctx ->
            if (ohlcDumpSubscriptionNeeded) {
                jobsList = enableOhlcDumping(cfg, ctx.marketDataDistributor, ioExecutor)
                ohlcDumpSubscriptionNeeded = false
            }
            executor.execute {
                ctx.backtest(endOfOptimize)
                reportProcessor.process(ctx.boundModels)
            }
            log.info("models scheduled for optimization ${ctx.boundModels.size}")
        }


        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.DAYS)

        jobsList.forEach { it.cancelAndJoin() }

        require(
            reportProcessor.bestModels().isNotEmpty(),
            { "no models get produced!! probably because they did not generated enough trades" })

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

    fun runSimple(mc: ModelConfig, runConfig : ModelBacktestConfig) {
        runSimple(listOf(mc), runConfig)
    }

    //fixme - provide backtest config separatele
    fun runSimple(mc: List<ModelConfig>, runConfig : ModelBacktestConfig) {

        clearReportDir(runConfig.reportTargetPath)

        val ctx = SimpleRunCtx(runConfig)

        mc.forEach {
            ctx.addModel(it.modelParams, it)
        }

        if (runConfig.dumpInterval != Interval.None) {
            enableOhlcDumping(
                config = runConfig,
                marketDataDistributor = ctx.marketDataDistributor,
                executorService = ioExecutor
            )
        }

        ctx.backtest(Instant.now())

        val trades = ctx.boundModels.flatMap { it.trades }
        val statuses =  ctx.boundModels.flatMap { it.orderStates }

        //fixme crap - need to change report to incorporate multiple models
        val output = ModelOutput(ctx.boundModels[0].model, mc[0].modelParams)
        output.trades += trades
        output.orderStates += statuses

        writeReport(output, runConfig)
    }

}


