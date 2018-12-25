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

    private val rollQueue = ArrayList<()->Unit>()


    override fun getOrCreateTs(idx: Int, interval: Interval, capacity: Int): TimeSeries<Ohlc> {
        if (!timeseries[idx].contains(interval)) {
            return createTimeSeries(idx, interval, capacity)
        }
        val hist = timeseries[idx][interval]
        if(capacity > hist.capacity() ){
            hist.adjustCapacity(capacity)
        }
        return hist
    }

    fun roll(dt : Instant){
        intervalService.onStep(dt)
        rollQueue.forEach {it()}
        rollQueue.clear()
    }

    private fun createTimeSeries(idx: Int, interval: Interval, length: Int = DEFAULT_TIME_SERIES_HISTORY_LENGTH): TimeSeriesImpl<Ohlc> {
        val timeSeries = TimeSeriesImpl(length) { Ohlc() }

        timeseries[idx][interval] = timeSeries

        intervalService.addListener(interval) {time->
            timeSeries.channel.publish(timeSeries)
            rollQueue += {
                timeSeries += timeSeries[0].copy(dtGmtEnd = time.plusMillis(interval.durationMs), interpolated = true)
            }
        }
        return timeSeries
    }

}