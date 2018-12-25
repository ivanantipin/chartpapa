package com.firelib.test


import firelib.common.interval.Interval
import firelib.common.model.Model
import firelib.common.model.ModelContext
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import java.time.Instant
import java.time.temporal.ChronoUnit

class OhlcTestModel(val context: ModelContext) : Model {
    override fun name(): String {
        return "OhlcModel"
    }

    var endTime = Instant.MIN


    val startTimesGmt = ArrayList<Instant>();


    private var hist: TimeSeries<Ohlc>? = null


    var dayHist: TimeSeries<Ohlc>? = null


    val uniqTimes = HashSet<Instant>()


    val bars = ArrayList<Ohlc>()


    private var omanagers: List<OrderManagerImpl> = context.instruments
            .map { OrderManagerImpl(context.tradeGate, context.timeService, it) }


    init {
        testHelper.instanceOhlc = this
        hist = context.mdDistributor.getOrCreateTs(0, Interval.Min5, 10)
        hist!!.preRollSubscribe { it -> on5Min(it) }
        dayHist = context.mdDistributor.getOrCreateTs(0, Interval.Day, 10)
    }


    override fun orderManagers(): List<OrderManager> {
        return omanagers
    }

    override fun update() {
    }

    override fun properties(): Map<String, String> {
        return emptyMap()
    }

    fun on5Min(ts: TimeSeries<Ohlc>): Unit {
        if (dayHist!!.count() > 0 && dayHist!![0].dtGmtEnd.truncatedTo(ChronoUnit.DAYS) != dayHist!![0].dtGmtEnd) {
            throw Exception("time of day ts not correct");
        }
        val currentTime = context.timeService.currentTime()
        if (currentTime != ts[0].time()) {
            throw Exception("time is not equal $currentTime <> ${ts[0].time()}");
        }
        bars += ts[0].copy()

        if (bars.size > 1) {
            if ((ts[0].dtGmtEnd.toEpochMilli() - ts[1].dtGmtEnd.toEpochMilli()) != 5 * 60 * 1000L) {
                throw Exception("not 5 min diff " + ts[0].dtGmtEnd + " -- " + ts[1].dtGmtEnd);
            }
        }
        addOhlc(ts[0]);
    }

    fun addOhlc(pQuote: Ohlc) {
        if (uniqTimes.contains(pQuote.dtGmtEnd)) {
            throw Exception("dupe time " + pQuote.dtGmtEnd);
        }
        uniqTimes.add(pQuote.dtGmtEnd)

        if (startTimesGmt.size == 0 || startTimesGmt.last().truncatedTo(ChronoUnit.DAYS) != pQuote.time().truncatedTo(ChronoUnit.DAYS)) {
            startTimesGmt += pQuote.time()
        }
    }

    override fun onBacktestEnd(): Unit {}
}
