package com.firelib.techbot.marketdata

import com.firelib.techbot.InflightHandler.registerInflight
import com.firelib.techbot.Misc.batchedCollect
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.marketdata.MdDaoExt.insertOhlcSuspend
import com.firelib.techbot.marketdata.MdDaoExt.truncateSuspend
import firelib.core.HistoricalSource
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.atUtc
import firelib.core.misc.toInstantDefault
import firelib.core.store.MdDao
import firelib.core.store.MdStorageImpl
import firelib.iqfeed.ContinousOhlcSeries
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class SeriesContainer(val dao: MdDao, val historicalSource: HistoricalSource, val instrId: InstrId) {

    private val tf2data = ConcurrentHashMap<Interval, TfOhlcs>()

    val log = LoggerFactory.getLogger(javaClass)

    val mutex = Mutex()

    suspend fun reset() {
        mutex.withLock {
            dao.truncateSuspend(instrId)
            tf2data.clear()
        }
    }

    private class TfOhlcs(
        @Volatile var data: List<Ohlc>,
        val series: ContinousOhlcSeries
    )

    fun getOhlcForTimeframe(interval: Interval): List<Ohlc> {
        return tf2data.computeIfAbsent(interval, {
            val series = initSeries(instrId, interval)
            TfOhlcs(series.data, series)
        }).data
    }

    private fun initSeries(ticker: InstrId, timeFrame: Interval): ContinousOhlcSeries {
        val transformingSeries = ContinousOhlcSeries(timeFrame)
        dao.queryAll(MdStorageImpl.makeTableName(ticker), getStartTime(timeFrame))
            .chunked(500).forEach {
                transformingSeries.add(it)
            }
        return transformingSeries
    }

    suspend fun sync() {
        mutex.withLock {
            var empty = false
            var latestFetched = dao.queryLast(instrId)?.endTime?.atUtc() ?: getStartTime(TimeFrame.W.interval)
            log.info("synching ${instrId.code} last fetched time is ${latestFetched}")
            while (!empty) {
                empty = true
                registerInflight("${historicalSource.getName()}_LOAD"){
                    historicalSource.getAsyncInterface()!!.load(instrId, latestFetched, Interval.Min10)
                        .filter { it.endTime > latestFetched.toInstantDefault() }
                        .batchedCollect(5000) { ohlcs ->
                            dao.insertOhlcSuspend(ohlcs, instrId)
                            latestFetched = ohlcs.last().endTime.atUtc()
                            log.info("updated ${instrId.code} last fetched time is ${latestFetched}")
                            add(ohlcs)
                            empty = false
                        }
                }

            }
        }
    }

    private fun add(ohlc: List<Ohlc>) {
        tf2data.forEach { (timeFrame, v) ->

            val triggerTimeToTrim = getStartTime(timeFrame).minusDays(30)

            v.series.add(ohlc)

            if( v.series.startTime() < triggerTimeToTrim.toInstantDefault()){
                val startTime = getStartTime(timeFrame).toInstantDefault()
                log.info("trimming series ${instrId} timeframe ${timeFrame} to start time ${startTime}")
                v.series.trimStart(startTime)
                log.info("start time after trimming is ${v.series.startTime()}")
            }

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