package com.firelib.techbot.staticdata

import com.firelib.techbot.persistence.BotConfig
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.misc.atUtc
import firelib.core.misc.toInstantDefault
import firelib.core.store.MdDao
import firelib.core.store.MdStorageImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

fun getStartTime(timeFrame: Interval) : LocalDateTime{
    return LocalDateTime.now().minus(timeFrame.duration.multipliedBy(BotConfig.window))
}

class OhlcsService(
    val daoProvider: (source: SourceName, interval: Interval) -> MdDao,
    val sourceProvider: (source: SourceName) -> HistoricalSource, val window: Long = BotConfig.window,
    val subscriptionService: SubscriptionService
) {

    fun start(){
        subscriptionService.addListener {
            launchFlowIfNeeded(it)
        }
    }

    val baseFlows = ConcurrentHashMap<InstrId, SeriesContainer>()

    val scope = CoroutineScope(Dispatchers.IO)

    fun CoroutineScope.launchFlow(instrId: InstrId) : SeriesContainer {

        val ret = SeriesContainer(daoProvider(instrId.sourceEnum(), Interval.Min10), instrId)

        val dataLoader = sourceProvider(instrId.sourceEnum())

        val dao = daoProvider(instrId.sourceEnum(), Interval.Min10)

        val last = dao.queryLast(instrId)?.endTime ?: getStartTime(Interval.Week).toInstantDefault()

        var latestFetched = last.atUtc()

        launch {

            while (true) {
                val lload = dataLoader.load(instrId, latestFetched, Interval.Min10)
                    .filter { it.endTime > latestFetched.toInstantDefault() }
                    .chunked(5000)

                var empty = true

                lload.forEach { ohlcs ->
                    dao.insertOhlc(ohlcs, instrId)
                    ret.add(ohlcs)
                    latestFetched = ohlcs.last().endTime.atUtc()
                    empty = false
                }

                if (empty) {
                    delay(60_000)
                }
            }
        }
        return ret
    }

    fun getOhlcsForTf(ticker: InstrId, timeFrame: Interval): StateFlow<List<Ohlc>> {
        val persistFlow = launchFlowIfNeeded(ticker)
        return persistFlow.getFlow(timeFrame)
    }

    fun launchFlowIfNeeded(ticker: InstrId): SeriesContainer {
        val persistFlow = baseFlows.computeIfAbsent(ticker, {
            scope.launchFlow(ticker)
        })
        return persistFlow
    }
}

