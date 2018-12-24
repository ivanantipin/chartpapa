package firelib.common.core

import firelib.common.MarketDataType
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.model.SmaFactory
import firelib.common.report.clearReportDir
import firelib.common.report.writeReport

fun runSimple(cfg: ModelBacktestConfig, fac : ModelFactory) {
    clearReportDir(cfg.reportTargetPath)

    val ctx = SimpleRunCtx(cfg)

    ctx.addModel(fac,cfg.modelParams)

    val outputs = ctx.backtest.backtest()

    assert(outputs.size == 1)

    writeReport(outputs[0], cfg, cfg.reportTargetPath)
}

fun main(args : Array<String>){
    val conf = ModelBacktestConfig()
    conf.dataServerRoot = "/ddisk/globaldatabase/"
    conf.reportTargetPath = "./report"
    conf.instruments = listOf(InstrumentConfig("aapl","1MIN/STK/AAPL_1.csv", MarketDataType.Ohlc))
    conf.modelParams = mapOf("long_ma" to "10", "short_ma" to "5")
    conf.precacheMarketData = false
    runSimple(conf, SmaFactory())
    println("some")
}
