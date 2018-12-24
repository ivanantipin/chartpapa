package firelib.common.mddistributor

import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.interval.IntervalServiceImpl
import firelib.common.timeseries.TimeSeries
import firelib.common.timeseries.TimeSeriesImpl
import firelib.domain.Ohlc
import java.time.Instant


class MarketDataDistributorImpl(
        val modelConfig: ModelBacktestConfig
) : MarketDataDistributor {


    val DEFAULT_TIME_SERIES_HISTORY_LENGTH = 100

    val timeseries = Array(modelConfig.instruments.size) { TimeSeriesContainer() }

    val intervalService = IntervalServiceImpl()

    override fun getOrCreateTs(idx: Int, interval: Interval, len: Int): TimeSeries<Ohlc> {
        if (!timeseries[idx].contains(interval)) {
            createTimeSeries(idx, interval, len)
        }
        val hist = timeseries[idx][interval]
        //fixme hist.adjustSizeIfNeeded(len)
        return hist
    }

    fun roll(dt : Instant){
        intervalService.onStep(dt)
    }

    private fun createTimeSeries(idx: Int, interval: Interval, len: Int): TimeSeriesImpl<Ohlc> {
        val length = if (len == -1) DEFAULT_TIME_SERIES_HISTORY_LENGTH else len

        val timeSeries = TimeSeriesImpl(length) { Ohlc() }

        timeseries[idx][interval] = timeSeries

        intervalService.addListener(interval) {
            timeSeries += timeSeries[0].copy(dtGmtEnd = it.plusMillis(interval.durationMs))
        }
        return timeSeries
    }

    override fun listenOhlc(idx: Int, lsn: (Ohlc) -> Unit): Unit {
        //fixme ohlcListeners[idx].subscribe(lsn)
    }
}