package firelib.common.mddistributor

import java.time.Instant

import firelib.common.core.{ModelConfigContext, OnContextInited}
import firelib.common.interval.{Interval, IntervalServiceComponent}
import firelib.common.misc.{Channel, NonDurableChannel, SubChannel, ohlcUtils, utils}
import firelib.common.timeseries.{OhlcSeries, TimeSeriesImpl}
import firelib.domain.{Ohlc, Tick}


trait MarketDataDistributorComponent {


    this : IntervalServiceComponent with ModelConfigContext with OnContextInited =>

    val marketDataDistributor = new MarketDataDistributorImpl()


    class MarketDataDistributorImpl() extends MarketDataDistributor {


        val DEFAULT_TIME_SERIES_HISTORY_LENGTH = 100

        private val timeseries = Array.fill[TimeSeriesContainer](modelConfig.instruments.length)(new TimeSeriesContainer())

        var tickTransformFunction : (Tick) => Tick =_

        def setTickTransformFunction(fun : (Tick) => Tick) : Unit = {
            tickTransformFunction = fun
        }

        var tickListeners : Array[Channel[Tick]] = Array.fill[Channel[Tick]](modelConfig.instruments.length)(new NonDurableChannel[Tick]())
        var ohlcListeners : Array[Channel[Ohlc]] = Array.fill[Channel[Ohlc]](modelConfig.instruments.length)(new NonDurableChannel[Ohlc]())

        setTickTransformFunction(utils.instanceOfClass[Tick=>Tick](modelConfig.tickToTickFuncClass))

        def onOhlc(idx: Int, ohlc: Ohlc): Unit = {
            ohlcListeners(idx).publish(ohlc)
            timeseries(idx).addOhlc(ohlc)
        }

        def onTick(idx: Int, tick: Tick): Unit = {
            val ntick = tickTransformFunction(tick)
            timeseries(idx).addTick(ntick)
            tickListeners(idx).publish(ntick)
        }


        def activateOhlcTimeSeries(idx: Int, interval: Interval, len: Int): OhlcSeries = {
            if (!timeseries(idx).contains(interval)) {
                createTimeSeries(idx, interval, len)
            }
            val hist = timeseries(idx).getTs(interval)
            hist.adjustSizeIfNeeded(len)
            return hist
        }

        def preInitCurrentBars(time : Instant): Unit ={
            for(cont <- timeseries){
                for(ts <- cont.iterator){
                    ts._2(0).dtGmtEnd=ts._1.ceilTime(time)
                }
            }
        }

        private def createTimeSeries(idx: Int, interval: Interval, len: Int): TimeSeriesImpl[Ohlc] with OhlcSeries = {
            val lenn = if (len == -1) DEFAULT_TIME_SERIES_HISTORY_LENGTH else len

            val timeSeries = new TimeSeriesImpl[Ohlc](lenn, () => new Ohlc()) with OhlcSeries

            timeseries(idx).addTs(interval, timeSeries)

            intervalService.addListener(interval, (dt) => {
                val next = ohlcUtils.interpolate(timeSeries(0))
                next.dtGmtEnd = dt.plusMillis(interval.durationMs)
                timeSeries.add(next)
            })
            timeSeries
        }

        override def listenTicks(idx : Int, lsn : Tick=>Unit) : Unit = tickListeners(idx).subscribe(lsn)

        override def tickTopic(idx : Int) : SubChannel[Tick] = tickListeners(idx)

        override def listenOhlc(idx : Int, lsn : Ohlc=>Unit) : Unit = ohlcListeners(idx).subscribe(lsn)

        override def getTs(tickerId: Int, interval: Interval): OhlcSeries = timeseries(tickerId).getTs(interval)
    }

}
