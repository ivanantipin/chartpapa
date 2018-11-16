package firelib.common.core

import firelib.common.config.ModelBacktestConfig
import firelib.common.report.reportWriter

class BacktesterSimple  {


    def run(cfg: ModelBacktestConfig) : Unit = {
        reportWriter.clearReportDir(cfg.reportTargetPath)

        val ctx: SimpleRunCtx = new SimpleRunCtx(cfg)

        ctx.init()

        ctx.bindModelForParams(cfg.modelParams.toMap)

        val outputs = ctx.backtest.backtest()

        assert(outputs.length == 1)

        reportWriter.write(outputs(0), cfg, cfg.reportTargetPath)
    }
}
