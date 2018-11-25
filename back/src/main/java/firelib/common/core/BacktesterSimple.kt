package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.report.clearReportDir
import firelib.common.report.writeReport

fun runSimple(cfg: ModelBacktestConfig) : Unit {
    clearReportDir(cfg.reportTargetPath)

    val outputs = SimpleRunCtx(cfg).backtest.backtest()

    assert(outputs.size == 1)

    writeReport(outputs[0], cfg, cfg.reportTargetPath)
}
