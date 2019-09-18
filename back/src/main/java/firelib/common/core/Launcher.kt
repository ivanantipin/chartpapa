package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.config.OptResourceParams
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.opt.ParamsVariator
import firelib.common.report.*
import firelib.common.threading.ThreadExecutorImpl
import firelib.common.timeboundscalc.TimeBoundsCalculatorImpl
import firelib.domain.Ohlc
import kotlinx.coroutines.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.LinkedBlockingQueue

object Launcher{

    suspend fun runOptimized(cfg: ModelBacktestConfig, factory : ModelFactory) {
        println("Starting")

        val startTime = System.currentTimeMillis()

        val reportProcessor = ReportProcessor(::statCalculator,
                cfg.optConfig.optimizedMetric,
                cfg.optConfig.params.map {it.name},
                minNumberOfTrades = cfg.optConfig.minNumberOfTrades)

        val variator = ParamsVariator(cfg.optConfig.params)

        val (startDtGmt, endDtGmt) = TimeBoundsCalculatorImpl()(cfg)

        val endOfOptimize =  if(cfg.optConfig.optimizedPeriodDays < 0) endDtGmt.plusMillis(100)
        else startDtGmt.plus(cfg.optConfig.optimizedPeriodDays, ChronoUnit.DAYS)

        println("total number of models to test : ${variator.combinations()}")

        val optResourceParams: OptResourceParams = cfg.optConfig.resourceStrategy.getParams(variator.combinations())

        val executor = ThreadExecutorImpl(optResourceParams.threadCount).start()

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
                val outputs = ctx.backtest(endOfOptimize)
                reportProcessor.process(outputs)

            }
            println("models scheduled for optimization ${ctx.boundModels.size}")

        }

        executor.shutdown()

        jobsList.forEach {it.cancelAndJoin()}

        require(reportProcessor.bestModels().isNotEmpty(), {"no models get produced!!"})

        var output = reportProcessor.bestModels().last()


//        if(endOfOptimize.isBefore(endDtGmt)){
//            val env = SimpleRunCtx(cfg)
//            val outputSeq= env.backtest(endOfOptimize)
//            assert(outputSeq.size == 1)
//            output = outputSeq[0]
//        }

        println("Model optimized in ${(System.currentTimeMillis() - startTime) / 1000} sec")

        writeReport(output, cfg)

        OptWriter.write(cfg.getReportDbFile(), reportProcessor.estimates)

        println("Finished")
    }

    private fun nextModelVariationsChunk(cfg: ModelBacktestConfig, variator: ParamsVariator, batchSize: Int, factory: ModelFactory): SimpleRunCtx {
        val env = SimpleRunCtx(cfg)

        while (variator.hasNext()) {
            var opts = variator.next()

            println("added params for opt ${opts}")

            val nm = HashMap(cfg.modelParams)
            nm.putAll(opts.entries.associateBy ({it.key},{it.value.toString()}))
            env.addModel(factory, nm)

            if (env.boundModels.size >= batchSize) {
                return env
            }
        }
        return env
    }

    suspend fun runSimple(cfg: ModelBacktestConfig, fac : ModelFactory) {

        clearReportDir(cfg.reportTargetPath)

        val ctx = SimpleRunCtx(cfg)

        ctx.addModel(fac, cfg.modelParams)


        var jobsList = emptyList<Job>()

        if(cfg.dumpOhlc){
            jobsList = subscribeToDumpOhlc(config = cfg, marketDataDistributor = ctx.marketDataDistributor)
        }

        val outputs = ctx.backtest(Instant.MAX)

        require(outputs.size == 1)

        if(cfg.dumpOhlc ){
            jobsList.forEach {it.cancelAndJoin()}
        }

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