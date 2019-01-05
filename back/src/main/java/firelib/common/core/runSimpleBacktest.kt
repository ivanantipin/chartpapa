package firelib.common.core

import firelib.common.MarketDataType
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.model.SmaFactory
import firelib.common.report.clearReportDir
import firelib.common.report.writeReport
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun runSimple(cfg: ModelBacktestConfig, fac : ModelFactory) {
    clearReportDir(cfg.reportTargetPath)

    val ctx = SimpleRunCtx(cfg)

    ctx.addModel(fac,cfg.modelParams)

    val outputs = ctx.backtest(Instant.MAX)

    assert(outputs.size == 1)

    writeReport(outputs[0], cfg, cfg.reportTargetPath)
}

fun main(args : Array<String>){
    val conf = ModelBacktestConfig()
    conf.dataServerRoot = "/ddisk/globaldatabase/"
    conf.reportTargetPath = "./report"
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
