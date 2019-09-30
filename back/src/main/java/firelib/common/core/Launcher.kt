package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.config.OptResourceParams
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.model.Model
import firelib.common.opt.ParamsVariator
import firelib.common.report.OhlcStreamWriter
import firelib.common.report.ReportProcessor
import firelib.common.report.ReportWriter
import firelib.common.report.ReportWriter.clearReportDir
import firelib.common.report.ReportWriter.writeReport
import firelib.common.report.statCalculator
import firelib.common.timeboundscalc.BacktestPeriodCalc
import firelib.domain.Ohlc
import kotlinx.coroutines.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Launcher{

    init{
        Thread.setDefaultUncaughtExceptionHandler({thread, throwable ->
            throwable.printStackTrace()
        })
    }

    suspend fun runOptimized(cfg: ModelBacktestConfig, factory : ModelFactory) {
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


        val executor = (Executors.newFixedThreadPool(optResourceParams.threadCount) as ThreadPoolExecutor).apply {
            rejectedExecutionHandler = ThreadPoolExecutor.CallerRunsPolicy()
        }

        clearReportDir(cfg.reportTargetPath)

        var ohlcDumpSubscriptionNeeded = cfg.dumpOhlc

        var jobsList = emptyList<Job>()

        while (variator.hasNext()) {
            val ctx = nextModelVariationsChunk(cfg, variator, optResourceParams.batchSize, factory)

            if(ohlcDumpSubscriptionNeeded){
                jobsList = subscribeToDumpOhlc(cfg,ctx.marketDataDistributor)
                ohlcDumpSubscriptionNeeded = false
            }

            executor.execute {
                reportProcessor.process(ctx.backtest(endOfOptimize))
            }
            println("models scheduled for optimization ${ctx.boundModels.size}")

        }

        executor.shutdown()
        executor.awaitTermination(1,TimeUnit.DAYS)

        jobsList.forEach {it.cancelAndJoin()}

        require(reportProcessor.bestModels().isNotEmpty(), {"no models get produced!!"})



//        if(endOfOptimize.isBefore(endDtGmt)){
//            val env = SimpleRunCtx(cfg)
//            val outputSeq= env.backtest(endOfOptimize)
//            assert(outputSeq.size == 1)
//            output = outputSeq[0]
//        }

        println("Model optimized in ${(System.currentTimeMillis() - startTime) / 1000} sec")

        writeReport(reportProcessor.bestModels().last(), cfg)

        ReportWriter.writeOpt(cfg.getReportDbFile(), reportProcessor.estimates)

        println("Finished")
    }

    private fun nextModelVariationsChunk(cfg: ModelBacktestConfig, variator: ParamsVariator, batchSize: Int, factory: ModelFactory): SimpleRunCtx {
        val env = SimpleRunCtx(cfg)

        while (variator.hasNext()) {
            var opts = variator.next()

            println("added params for opt ${opts}")

            env.addModel(factory, cfg.modelParams + opts.mapValues { "${it.value}"})

            if (env.boundModels.size >= batchSize) {
                return env
            }
        }
        return env
    }

    suspend fun runSimple(cfg: ModelBacktestConfig, fac: ModelFactory, ctxListener: (Model) -> Unit = {}) {

        clearReportDir(cfg.reportTargetPath)

        val ctx = SimpleRunCtx(cfg).apply {
            val model = addModel(fac, cfg.modelParams)
            ctxListener(model)
        }

        var jobsList = emptyList<Job>()

        if(cfg.dumpOhlc){
            jobsList = subscribeToDumpOhlc(config = cfg, marketDataDistributor = ctx.marketDataDistributor)
        }

        val outputs = ctx.backtest(Instant.MAX)

        require(outputs.size == 1)

        jobsList.forEach {it.cancelAndJoin()}

        writeReport(outputs[0], cfg)

        println("done")
    }


    fun subscribeToDumpOhlc(config: ModelBacktestConfig, marketDataDistributor: MarketDataDistributor) : List<Job> {
        return config.instruments.mapIndexed{ instrIdx, _ ->
            val ohlcPath = config.getReportDbFile()
            val writer = OhlcStreamWriter(ohlcPath)
            val ts = marketDataDistributor.getOrCreateTs(instrIdx, Interval.Min240, 2)

            val ticker = config.instruments[instrIdx].ticker

            val queue = LinkedBlockingQueue<Ohlc>()

            ts.preRollSubscribe { queue.add(it[0]) }

            GlobalScope.launch {
                while (isActive){
                    val list = ArrayList<Ohlc>()
                    queue.drainTo(list)
                    if(!list.isEmpty()){
                        println("start inserting ${list.size}")
                        writer.insertOhlcs("ohlc_$ticker", list)
                    }
                    delay(100)
                }
            }
        }
    }
}