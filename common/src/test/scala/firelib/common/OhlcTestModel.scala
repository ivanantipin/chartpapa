package firelib.common

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util

import firelib.common.interval.Interval
import firelib.common.misc.ohlcUtils
import firelib.common.model.BasketModel
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc

import scala.collection.mutable.ArrayBuffer

class OhlcTestModel extends BasketModel {

    var endTime = Instant.MIN


    val startTimesGmt = new ArrayBuffer[Instant]();


    private var hist: TimeSeries[Ohlc] = _


    var dayHist: TimeSeries[Ohlc] = _


    val uniqTimes = new util.HashSet[Instant]()


    val bars = new ArrayBuffer[Ohlc]()


    override def applyProperties(mprops: Map[String, String]) : ModelInitResult = {
        testHelper.instanceOhlc = this
        hist = enableOhlc(Interval.Min5, 10)(0);
        Interval.Min5.listen(On5Min)
        dayHist = enableOhlc(Interval.Day, 10)(0);
        ModelInitResult.Success
    }


    def On5Min(il: Interval): Unit = {
        val hh = il.getOhlc(0)
        if (dayHist.count > 0 && dayHist(0).dtGmtEnd.truncatedTo(ChronoUnit.DAYS) != dayHist(0).dtGmtEnd) {
            throw new Exception("time of day ts not correct");
        }

        if (currentTime != hh(0).time) {
            throw new Exception(s"time is not equal $currentTime <> ${hh(0).time}");
        }
        bars +=  ohlcUtils.copy(hh(0
        )
        )

        if (bars.size > 1) {
            if ((hh(0).getDtGmtEnd.toEpochMilli - hh(1).getDtGmtEnd.toEpochMilli) != 5*60*1000) {
                throw new Exception("not 5 min diff " + hh(0).getDtGmtEnd + " -- " + hh(1).getDtGmtEnd);
            }
        }
        AddOhlc(ohlcUtils.copy(hh(0)));
    }

    def AddOhlc(pQuote: Ohlc) = {
        if (uniqTimes.contains(pQuote.dtGmtEnd)) {
            throw new Exception("dupe time " + pQuote.dtGmtEnd);
        }
        uniqTimes.add(pQuote.getDtGmtEnd)

        if (startTimesGmt.size == 0 || startTimesGmt.last.truncatedTo(ChronoUnit.DAYS) != pQuote.time.truncatedTo(ChronoUnit.DAYS)) {
            startTimesGmt += pQuote.time
        }
    }

    override def onBacktestEnd(): Unit = {}
}
