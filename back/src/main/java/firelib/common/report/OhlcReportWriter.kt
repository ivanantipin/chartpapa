package firelib.common.report

import firelib.common.OrderStatus
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.misc.WindowSlicer
import firelib.common.model.Model
import firelib.domain.Ohlc
import java.nio.file.Paths
import java.time.Duration


fun subscribeToDumpOhlc(model: Model, minsWindow: Int = 100, config : ModelBacktestConfig, marketDataDistributor : MarketDataDistributor){
    for(instrIdx in 0..config.instruments.size)
    {
        val slicer = WindowSlicer<Ohlc>(Duration.ofMinutes(minsWindow.toLong()))
        model.orderManagers()[instrIdx]!!.tradesTopic().subscribe {slicer.updateWriteBefore()}
        model.orderManagers()[instrIdx]!!.orderStateTopic().filter {it.status == OrderStatus.New}.subscribe {slicer.updateWriteBefore()}
        val ts = marketDataDistributor.getOrCreateTs(instrIdx, Interval.Min1, minsWindow)
        val ohlcPath = Paths.get(config.reportTargetPath).resolve("ohlc_${config.instruments[instrIdx].ticker}.csv")
        ts.subscribe { OhlcStreamWriter (ohlcPath)}
    }
}

/*
if(modelConfig.dumpOhlcData && modelConfig.backtestMode == BacktestMode.SimpleRun)
{
    onModelBinded.subscribe(m->{
    assert(mdWriter == null)
    mdWriter = OhlcReportWriter(m)
})
}
*/

