package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.staticdata.getStartTime
import firelib.core.HistoricalSource
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.atUtc
import firelib.core.misc.toInstantDefault
import firelib.core.store.MdDao
import firelib.core.store.MdStorageImpl
import firelib.iqfeed.ContinousOhlcSeries
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class SeriesContainer(val dao: MdDao, val dataLoader : HistoricalSource, val instrId: InstrId) {

    private val tf2data = ConcurrentHashMap<Interval, TfOhlcs>()

    val log = LoggerFactory.getLogger(javaClass)

    @Synchronized
    fun reset(){
        dao.truncate(instrId)
        tf2data.clear()
    }

    class TfOhlcs(
        @Volatile var data: List<Ohlc>,
        val series: ContinousOhlcSeries)

    fun getOhlcForTimeframe(interval: Interval): List<Ohlc> {
        return tf2data.computeIfAbsent(interval, {
            val series = initSeries(instrId, interval)
            TfOhlcs(series.data, series)
        }).data
    }

    @Synchronized
    fun initSeries(ticker: InstrId, timeFrame: Interval): ContinousOhlcSeries {
        val transformingSeries = ContinousOhlcSeries(timeFrame)
        dao.queryAll(MdStorageImpl.makeTableName(ticker), getStartTime(timeFrame))
            .chunked(500).forEach {
                transformingSeries.add(it)
            }
        return transformingSeries
    }


    @Synchronized
    fun sync(){
        var empty = false
        var latestFetched = dao.queryLast(instrId)?.endTime?.atUtc() ?: getStartTime(TimeFrame.W.interval)
        while (!empty){
            empty = true
            val ohs = dataLoader.load(instrId, latestFetched, Interval.Min10)
                .filter { it.endTime > latestFetched.toInstantDefault() }
                .chunked(5000)
            ohs.forEach { ohlcs ->
                dao.insertOhlc(ohlcs, instrId)
                latestFetched = ohlcs.last().endTime.atUtc()
                add(ohlcs)
                empty = false
            }
        }
    }

    private fun add(ohlc: List<Ohlc>) {

        tf2data.forEach { timeFrame, v ->

            v.series.add(ohlc)

            val maxAllowedReminder = when (timeFrame) {
                Interval.Day, Interval.Week -> {
                    0.25
                }
                else -> 0.001
            }

            val series = v.series
            val last = series.data.last()

            val reminder = (last.endTime.toEpochMilli() - series.lastFetchedTs) / timeFrame.durationMs
            val completeBar = reminder <= maxAllowedReminder
            val copy = mutableListOf<Ohlc>().apply {
                addAll(series.data)
            }
            v.data = if (completeBar) {
                copy
            } else {
                copy.subList(0, copy.size - 1)
            }
        }

    }

}