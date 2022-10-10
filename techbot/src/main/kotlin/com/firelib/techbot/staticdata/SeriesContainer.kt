package com.firelib.techbot.staticdata

import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.store.MdDao
import firelib.core.store.MdStorageImpl
import firelib.iqfeed.ContinousOhlcSeries
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class SeriesContainer(val dao: MdDao, val instrId: InstrId) {

    val tf2data = ConcurrentHashMap<Interval, FlowAndSeries>()

    val completed = CompletableFuture<Boolean>()

    class FlowAndSeries(val flow: MutableStateFlow<List<Ohlc>>, val series: ContinousOhlcSeries)

    fun getFlowForTimeframe(interval: Interval): MutableStateFlow<List<Ohlc>> {
        return tf2data.computeIfAbsent(interval, {
            val series = initSeries(instrId, interval)
            FlowAndSeries(MutableStateFlow(series.data), series)
        }).flow
    }

    fun initSeries(ticker: InstrId, timeFrame: Interval): ContinousOhlcSeries {
        val transformingSeries = ContinousOhlcSeries(timeFrame)

        dao.queryAll(MdStorageImpl.makeTable(ticker), getStartTime(timeFrame))
            .chunked(500).forEach {
                transformingSeries.add(it)
            }
        return transformingSeries

    }

    fun add(ohlc: List<Ohlc>) {

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
            v.flow.value = if (completeBar) {
                copy
            } else {
                copy.subList(0, copy.size - 1)
            }
        }

    }

}