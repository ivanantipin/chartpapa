package firelib.common.mddistributor

import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.interval.IntervalService
import firelib.common.misc.Channel
import firelib.common.misc.NonDurableChannel
import firelib.common.timeseries.TimeSeries
import firelib.common.timeseries.TimeSeriesImpl
import firelib.domain.Ohlc


class MarketDataDistributorImpl(
        val modelConfig: ModelBacktestConfig,
        val intervalService: IntervalService

) : MarketDataDistributor {


    val DEFAULT_TIME_SERIES_HISTORY_LENGTH = 100

    private val timeseries = Array(modelConfig.instruments.size) { TimeSeriesContainer() }

    var ohlcListeners: Array<Channel<Ohlc>> = Array(modelConfig.instruments.size) { NonDurableChannel<Ohlc>() }

    fun onOhlc(idx: Int, ohlc: Ohlc): Unit {
        ohlcListeners[idx].publish(ohlc)
        timeseries[idx].addOhlc(ohlc)
    }

    override fun activateOhlcTimeSeries(idx: Int, interval: Interval, len: Int): TimeSeries<Ohlc> {
        if (!timeseries[idx].contains(interval)) {
            createTimeSeries(idx, interval, len)
        }
        val hist = timeseries[idx].getTs(interval)
        //fixme hist.adjustSizeIfNeeded(len)
        return hist
    }

/*
    fun preInitCurrentBars(time: Instant): Unit {
        for (cont in timeseries) {
            for (ts in cont.iterator) {
                ts._2(0).dtGmtEnd = ts._1.ceilTime(time)
            }
        }
    }
*/

    private fun createTimeSeries(idx: Int, interval: Interval, len: Int): TimeSeriesImpl<Ohlc> {
        val lenn = if (len == -1) DEFAULT_TIME_SERIES_HISTORY_LENGTH else len

        val timeSeries = TimeSeriesImpl<Ohlc>(lenn) { Unit -> Ohlc() }

        timeseries[idx].addTs(interval, timeSeries)

        intervalService.addListener(interval, { dt ->
            timeSeries.add(timeSeries[0].copy(dtGmtEnd = dt.plusMillis(interval.durationMs)))
        })
        return timeSeries
    }

    override fun listenOhlc(idx: Int, lsn: (Ohlc) -> Unit): Unit {
        ohlcListeners[idx].subscribe(lsn)
    }

    override fun getTs(tickerId: Int, interval: Interval): TimeSeries<Ohlc> {
        return timeseries[tickerId].getTs(interval)
    }
}