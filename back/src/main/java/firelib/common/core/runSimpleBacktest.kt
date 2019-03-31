package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.model.Model
import firelib.common.report.OhlcStreamWriter
import firelib.common.report.clearReportDir
import firelib.common.report.writeReport
import firelib.domain.Ohlc
import kotlinx.coroutines.*
import java.nio.file.Paths
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue


suspend fun runSimple(cfg: ModelBacktestConfig, fac : ModelFactory) {

    clearReportDir(cfg.reportTargetPath)

    val ctx = SimpleRunCtx(cfg)

    val model = ctx.addModel(fac, cfg.modelParams)

    //val persistJobs = subscribeToDumpOhlc(model = model, config = cfg, marketDataDistributor = ctx.marketDataDistributor)
    val outputs = ctx.backtest(Instant.MAX)

    require(outputs.size == 1)

    //persistJobs.forEach {it.cancelAndJoin()}

    writeReport(outputs[0], cfg)


}


fun subscribeToDumpOhlc(model: Model, minsWindow: Int = 10, config : ModelBacktestConfig, marketDataDistributor : MarketDataDistributor) : List<Job> {
    return config.instruments.mapIndexed{instrIdx, ins->
        val ohlcPath = Paths.get(config.reportTargetPath).resolve("report.db")
        val writer = OhlcStreamWriter(ohlcPath)
        val ts = marketDataDistributor.getOrCreateTs(instrIdx, Interval.Min240, minsWindow)

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



