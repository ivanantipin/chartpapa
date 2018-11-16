package firelib.common.core

import firelib.common.config.ModelBacktestConfig

/**

 */
object backtestStarter {
    def runBacktest(mc: ModelBacktestConfig) {
        try{
            val start: Long = System.currentTimeMillis()
            mc.backtestMode match {
                case BacktestMode.Optimize => new BacktesterOptimized().run(mc)
                case BacktestMode.SimpleRun => new BacktesterSimple().run(mc)
                case BacktestMode.FwdTesting => throw new RuntimeException("fwd testing not supported yet")
                case _=>throw new RuntimeException("not possible")
            }
            System.out.println(s"backtest finished in ${(System.currentTimeMillis() - start)/1000.0} s")

        }catch {
            case ex : Throwable => ex.printStackTrace()
        }
    }
}
