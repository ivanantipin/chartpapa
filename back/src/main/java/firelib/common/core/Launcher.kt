package firelib.common.core

import firelib.common.Order
import firelib.common.OrderStatus
import firelib.common.Trade
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.OptResourceParams
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.common.model.Model
import firelib.common.opt.ParamsVariator
import firelib.common.report.*
import firelib.common.report.ReportWriter.clearReportDir
import firelib.common.report.ReportWriter.writeReport
import firelib.common.report.dao.OhlcStreamWriter
import firelib.common.report.dao.StreamOrderWriter
import firelib.common.report.dao.StreamTradeCaseWriter
import firelib.common.timeboundscalc.BacktestPeriodCalc
import firelib.common.timeseries.nonInterpolatedView
import firelib.domain.Ohlc
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.*

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
        }.chunked(optResourceParams.batchSize).map {
            val ctx = SimpleRunCtx(cfg)
            it.forEach({ p ->
                factory(ctx.modelContext, p)
            })
            ctx
        }.forEach { ctx->
            if(ohlcDumpSubscriptionNeeded){
                jobsList = enableOhlcDumping(cfg,ctx.marketDataDistributor)
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

    private fun makeExecutor(optResourceParams: OptResourceParams): ThreadPoolExecutor {
        val executor = (Executors.newFixedThreadPool(optResourceParams.threadCount) as ThreadPoolExecutor).apply {
            rejectedExecutionHandler = ThreadPoolExecutor.CallerRunsPolicy()
        }
        return executor
    }

    fun runSimple(cfg: ModelBacktestConfig, fac: ModelFactory, ctxListener: (Model) -> Unit = {}) {

        clearReportDir(cfg.reportTargetPath)

        val ioExecutor = Executors.newSingleThreadExecutor()

        val batchers = mutableListOf<Batcher<out Any>>()

        val ctx = SimpleRunCtx(cfg)

        val model = ctx.addModel(fac, cfg.modelParams)

        batchers += enableOrdersPersist(model, cfg.getReportDbFile(),ioExecutor)
        batchers += enableTradeCasePersist(model, cfg.getReportDbFile(),ioExecutor)
        batchers += enableTradeRtPersist(model, cfg.getReportDbFile(),ioExecutor)
        ctxListener(model)
        if(cfg.dumpOhlc){
            batchers +=  enableOhlcDumping(config = cfg, marketDataDistributor = ctx.marketDataDistributor)
        }

        ReportWriter.copyPythonFiles(cfg)

        val outputs = ctx.backtest(Instant.MAX)

        require(outputs.size == 1)

        ReportWriter.writeStaticConf(cfg, outputs[1])

        println("report written to ${cfg.reportTargetPath} you can run it , command 'jupyter lab'")

        batchers.forEach {it.cancelAndJoin()}

        println("done")
    }

    fun enableTradeCasePersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService) : Batcher<Pair<Trade,Trade>>{
        val tradeCaseWriter = StreamTradeCaseWriter(reportFilePath)

        val casesBatcher = Batcher<Pair<Trade, Trade>>({
            ioExecutor.submit({tradeCaseWriter.insertTrades(it)}).get()
        }, "cases writer")


        model.orderManagers().forEach({om->
            val generator = StreamTradeCaseGenerator()
            om.tradesTopic().subscribe {
                casesBatcher.addAll(generator.genClosedCases(it))
            }
        })

        return casesBatcher
    }

    fun enableOrdersPersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService) : Batcher<Order>{
        val orderWriter = StreamOrderWriter(reportFilePath)

        val orderBatcher = Batcher<Order>({
            ioExecutor.submit({orderWriter.insertOrders(it)}).get()
        }, "cases writer")

        model.orderManagers().forEach({om->
            om.orderStateTopic().subscribe {
                if(it.status == OrderStatus.New){
                    orderBatcher.add(it.order)
                }
            }
        })
        return orderBatcher
    }



    fun enableTradeRtPersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService) : Batcher<Trade>{

        val tradeWriter = GenericDumper<Trade>("tradesRuntime", reportFilePath, Trade::class)

        val tradesBatcher = Batcher<Trade>({
            ioExecutor.submit({tradeWriter.write(it)}).get()}, "tradesRealtime")

        model.orderManagers().forEach({om->
            om.tradesTopic().subscribe {
                tradesBatcher.add(it)
            }
        })
        return tradesBatcher
    }


    fun enableOhlcDumping(config: ModelBacktestConfig, marketDataDistributor: MarketDataDistributor) : List<Batcher<Ohlc>> {
        return config.instruments.mapIndexed{ instrIdx, instr ->
            val writer = OhlcStreamWriter(config.getReportDbFile())
            val batcher = Batcher<Ohlc>({ writer.insertOhlcs(instr.ticker, it) }, instr.ticker)

            marketDataDistributor.getOrCreateTs(instrIdx, Interval.Min240, 2)
                    .nonInterpolatedView()
                    .preRollSubscribe({
                batcher.add(it[0])
            })

            batcher.apply {
                isDaemon = true
                start()
            }
        }
    }
}

