package com.firelib.test


import firelib.common.interval.Interval
import firelib.common.model.Model
import firelib.common.model.ModelContext
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import java.time.Instant
import java.time.temporal.ChronoUnit

class OhlcTestModel(context: ModelContext) : Model(context, emptyMap()) {

    val startTimesGmt = ArrayList<Instant>();


    private var hist: TimeSeries<Ohlc>


    var dayHist: TimeSeries<Ohlc>


    val uniqTimes = HashSet<Instant>()


    val bars = ArrayList<Ohlc>()


    init {
        testHelper.instanceOhlc = this
        hist = context.mdDistributor.getOrCreateTs(0, Interval.Min5, 10)
        hist.preRollSubscribe { on5Min(it) }
        dayHist = context.mdDistributor.getOrCreateTs(0, Interval.Day, 10)
    }


    fun on5Min(ts: TimeSeries<Ohlc>) {
        if (dayHist.count() > 0 && dayHist[0].endTime.truncatedTo(ChronoUnit.DAYS) != dayHist[0].endTime) {
            throw Exception("time of day ts not correct");
        }
        val currentTime = context.timeService.currentTime()
        if (currentTime != ts[0].endTime) {
            throw Exception("time is not equal $currentTime <> ${ts[0].endTime}");
        }
        bars += ts[0].copy()

        if (bars.size > 1) {
            if ((ts[0].endTime.toEpochMilli() - ts[1].endTime.toEpochMilli()) != 5 * 60 * 1000L) {
                throw Exception("not 5 min diff " + ts[0].endTime + " -- " + ts[1].endTime);
            }
        }
        addOhlc(ts[0]);
    }

    fun addOhlc(pQuote: Ohlc) {
        if (uniqTimes.contains(pQuote.endTime)) {
            throw Exception("dupe time " + pQuote.endTime);
        }
        uniqTimes.add(pQuote.endTime)

        if (startTimesGmt.size == 0 || startTimesGmt.last().truncatedTo(ChronoUnit.DAYS) != pQuote.endTime.truncatedTo(ChronoUnit.DAYS)) {
            startTimesGmt += pQuote.endTime
        }
    }

    override fun onBacktestEnd(): Unit {}
}
