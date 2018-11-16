package firelib.common

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util

import firelib.common.interval.Interval
import firelib.common.misc.ohlcUtils
import firelib.common.model.BasketModel
import firelib.common.timeseries.TimeSeries
import firelib.domain.{Ohlc, Tick}

import scala.collection.mutable.ArrayBuffer




class TickTestModel extends BasketModel {

    var endTime = Instant.MIN

    val daysStarts = new ArrayBuffer[Instant]()

    private var hist: TimeSeries[Ohlc] = _

    val uniqTimes = new util.HashSet[Instant]()


    override def applyProperties(mprops: Map[String, String]) : ModelInitResult = {
        testHelper.instanceTick = this

        hist = enableOhlc(Interval.Min5, 10)(0)
        Interval.Min5.listen(On5Min)


        bindComp.marketDataDistributor.listenTicks(0,onTick)
        ModelInitResult.Success
    }


    val bars = new ArrayBuffer[Ohlc]()

    private def On5Min(il: Interval): Unit = {
        bars += ohlcUtils.copy(il.getOhlc(0)(0))
        //System.out.println(hh(0))
    }


    var NumberOfTickes = 0

    def onOhlc(idx: Int, ohlc: Ohlc, next: Ohlc) = ???

    val ticks = new ArrayBuffer[Tick]()

    def onTick(tick: Tick) : Unit = {
        NumberOfTickes += 1
        assert(!uniqTimes.contains(tick.time),"dupe time " + tick.time)
        uniqTimes.add(tick.time)
        ticks += tick

        if (daysStarts.size == 0 || daysStarts.last.truncatedTo(ChronoUnit.DAYS) != tick.time.truncatedTo(ChronoUnit.DAYS)) {
            daysStarts += tick.time
        }
    }

    override def onBacktestEnd(): Unit = {}
}
