package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.config.OptResourceParams
import firelib.common.opt.ParamsVariator
import firelib.common.reader.ReaderFactoryImpl
import firelib.common.report.*
import firelib.common.threading.ThreadExecutorImpl
import firelib.common.timeboundscalc.TimeBoundsCalculatorImpl
import java.time.Instant
import java.time.temporal.ChronoUnit


fun runOptimized(cfg: ModelBacktestConfig, factory : ModelFactory): Unit {
    System.out.println("Starting")

    val startTime = System.currentTimeMillis()

    val reportProcessor = ReportProcessor(::statCalculator,
            cfg.optConfig.optimizedMetric,
            cfg.optConfig.params.map {it.name},
    minNumberOfTrades = cfg.optConfig.minNumberOfTrades)

    //FIXME investigate rejected tasks

    val reportExecutor = ThreadExecutorImpl(1).start()
    val variator = ParamsVariator(cfg.optConfig.params)

    val (startDtGmt, endDtGmt) = TimeBoundsCalculatorImpl()(cfg)

    val endOfOptimize =  if(cfg.optConfig.optimizedPeriodDays < 0) endDtGmt.plusMillis(100) else startDtGmt.plus(cfg.optConfig.optimizedPeriodDays, ChronoUnit.DAYS)

    println("number of models " + variator.combinations())

    val optResourceParams: OptResourceParams = cfg.optConfig.resourceStrategy.getParams(variator.combinations())

    val executor = ThreadExecutorImpl(optResourceParams.threadCount).start()

    while (variator.hasNext()) {
        val env = nextModelVariationsChunk(cfg, variator, optResourceParams.batchSize, factory)
        executor.execute {
            val outputs = env.backtest(endOfOptimize)
            reportExecutor.execute({reportProcessor.process(outputs)})
        }
        println("models scheduled for optimization ${env.boundModels.size}")

    }

    executor.shutdown()
    reportExecutor.shutdown()

    println("Model optimized in ${(System.currentTimeMillis() - startTime) / 1000} sec")


    assert(reportProcessor.bestModels().size > 0, {"no models get produced!!"})

    var output = reportProcessor.bestModels().last()

    clearReportDir(cfg.reportTargetPath)

    if(endOfOptimize.isBefore(endDtGmt)){
        val env = SimpleRunCtx(cfg)
        val outputSeq= env.backtest(endOfOptimize)
        assert(outputSeq.size == 1)
        output = outputSeq[0]
    }

    writeReport(output, cfg, cfg.reportTargetPath)

    writeOptimizedReport(cfg, reportProcessor, endOfOptimize)

    println("Finished")
}


private fun writeOptimizedReport(cfg: ModelBacktestConfig, reportProcessor: ReportProcessor, endOfOptimize: Instant) {
    optParamsWriter.write(
            cfg.reportTargetPath,
            optEnd = endOfOptimize,
            estimates = reportProcessor.estimates,
            optParams = cfg.optConfig.params,
            metrics = cfg.calculatedMetrics)
}


private fun nextModelVariationsChunk(cfg: ModelBacktestConfig, variator: ParamsVariator, batchSize: Int, factory: ModelFactory): SimpleRunCtx {
    val env = SimpleRunCtx(cfg)

    while (variator.hasNext()) {
        var opts = variator.next()
        val nm = HashMap(cfg.modelParams)
        nm.putAll(opts.entries.associateBy ({it.key},{it.value.toString()}))
        env.addModel(factory, nm)
        if (env.boundModels.size >= batchSize) {
            return env
        }
    }
    return env
}