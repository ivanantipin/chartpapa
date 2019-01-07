package firelib.common.core

import firelib.common.MarketDataType
import firelib.common.OrderStatus
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.model.Model
import firelib.common.model.SmaFactory
import firelib.common.report.OhlcStreamWriter
import firelib.common.report.clearReportDir
import firelib.common.report.writeReport
import firelib.domain.Ohlc
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun runSimple(cfg: ModelBacktestConfig, fac : ModelFactory) {
    clearReportDir(cfg.reportTargetPath)

    val ctx = SimpleRunCtx(cfg)

    val model = ctx.addModel(fac, cfg.modelParams)

    subscribeToDumpOhlc(model = model,config = cfg, marketDataDistributor = ctx.marketDataDistributor)

    val outputs = ctx.backtest(Instant.MAX)

    require(outputs.size == 1)

    writeReport(outputs[0], cfg, cfg.reportTargetPath)
}


fun subscribeToDumpOhlc(model: Model, minsWindow: Int = 10, config : ModelBacktestConfig, marketDataDistributor : MarketDataDistributor){
    for(instrIdx in 0 until config.instruments.size)
    {

        val ohlcPath = Paths.get(config.reportTargetPath).resolve("report.db")
        val writer = OhlcStreamWriter(ohlcPath)
        val ts = marketDataDistributor.getOrCreateTs(instrIdx, Interval.Min10, minsWindow)

        val ticker = config.instruments[instrIdx].ticker


        val subs: (Any) -> Unit = {

            val lst = ArrayList<Ohlc>();
            for (i in 0 until minsWindow) {
                lst += ts[i]
            }
            writer.insertOhlcs("ohlc_$ticker", lst)
        }
        model.orderManagers()[instrIdx]!!.tradesTopic().subscribe(subs)
        model.orderManagers()[instrIdx]!!.orderStateTopic().filter {it.status == OrderStatus.New}.subscribe {subs}
    }
}



fun main(args : Array<String>){
    val conf = ModelBacktestConfig()
    conf.dataServerRoot = "/ddisk/globaldatabase/"
    conf.reportTargetPath = "./report"
    conf.startDateGmt = LocalDateTime.now().minusDays(300).toInstant(ZoneOffset.UTC)
    conf.instruments = listOf(
            InstrumentConfig("aapl","1MIN/STK/AAPL_1.csv", MarketDataType.Ohlc),
            InstrumentConfig("goog","1MIN/STK/GOOG_1.csv", MarketDataType.Ohlc)
    )
    conf.modelParams = mapOf("period" to "10")
    //conf.startDateGmt = LocalDateTime.now().minusDays(600).toInstant(ZoneOffset.UTC)
    conf.precacheMarketData = false
    runSimple(conf, SmaFactory())
    println("some")
}
