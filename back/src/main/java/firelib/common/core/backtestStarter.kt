package firelib.common.core

import firelib.common.config.ModelBacktestConfig

/**

 */
fun runBacktest(mc: ModelBacktestConfig) {
    try{
        val start: Long = System.currentTimeMillis()
         when(mc.backtestMode) {
            //fixme BacktestMode.Optimize -> runOptimized(mc)
            BacktestMode.SimpleRun -> runSimple(mc)
            BacktestMode.FwdTesting -> throw RuntimeException("fwd testing not supported yet")
            else->throw RuntimeException("not possible")
        }
        System.out.println("backtest finished in ${(System.currentTimeMillis() - start)/1000.0} s")
    }catch(ex : Exception) {
        ex.printStackTrace()
    }
}
