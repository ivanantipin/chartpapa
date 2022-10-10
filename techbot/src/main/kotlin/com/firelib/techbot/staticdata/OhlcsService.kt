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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

fun getStartTime(timeFrame: Interval) : LocalDateTime{
    return LocalDateTime.now().minus(timeFrame.duration.multipliedBy(BotConfig.window))
}

class OhlcsService(
    val daoProvider: (source: SourceName, interval: Interval) -> MdDao,
    val sourceProvider: (source: SourceName) -> HistoricalSource, val window: Long = BotConfig.window
) {

    val log = LoggerFactory.getLogger(javaClass)


    val baseFlows = ConcurrentHashMap<InstrId, SeriesContainer>()

    val scope = CoroutineScope(Dispatchers.IO)

    fun CoroutineScope.launchFlow(instrId: InstrId) : SeriesContainer {

        val container = SeriesContainer(daoProvider(instrId.sourceEnum(), Interval.Min10), instrId)

        val dataLoader = sourceProvider(instrId.sourceEnum())

        val dao = daoProvider(instrId.sourceEnum(), Interval.Min10)

        val last = dao.queryLast(instrId)?.endTime ?: getStartTime(Interval.Week).toInstantDefault()

        log.info("flow launched from start time ${last}")

        var latestFetched = last.atUtc()

        launch {

            while (true) {
                val lload = dataLoader.load(instrId, latestFetched, Interval.Min10)
                    .filter { it.endTime > latestFetched.toInstantDefault() }
                    .chunked(5000)

                var empty = true

                lload.forEach { ohlcs ->
                    dao.insertOhlc(ohlcs, instrId)
                    container.add(ohlcs)
                    latestFetched = ohlcs.last().endTime.atUtc()
                    empty = false
                }

                if (empty) {
                    if(!container.completed.isDone){
                        container.completed.complete(true)
                    }
                    delay(60_000)
                }
            }
        }
        return container
    }

    fun getOhlcsForTf(ticker: InstrId, timeFrame: Interval): StateFlow<List<Ohlc>> {
        val persistFlow = launchFlowIfNeeded(ticker)
        return persistFlow.getFlowForTimeframe(timeFrame)
    }

    fun launchFlowIfNeeded(ticker: InstrId): SeriesContainer {
        val persistFlow = baseFlows.computeIfAbsent(ticker, {
            scope.launchFlow(ticker)
        })
        return persistFlow
    }
}

