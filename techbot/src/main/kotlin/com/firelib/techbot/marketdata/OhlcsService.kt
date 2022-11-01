package com.firelib.techbot.marketdata

import com.firelib.techbot.persistence.BotConfig
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.misc.timeSequence
import firelib.core.store.HistoricalSourceProvider
import firelib.core.store.MdDaoContainer
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

fun getStartTime(timeFrame: Interval): LocalDateTime {
    return LocalDateTime.now().minus(timeFrame.duration.multipliedBy(BotConfig.window))
}

class OhlcsService(
    val daoProvider: MdDaoContainer,
    val sourceProvider: HistoricalSourceProvider
) {

    val log = LoggerFactory.getLogger(javaClass)

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val baseFlows = ConcurrentHashMap<InstrId, SeriesContainer>()

    fun start(){
        CoroutineScope(Dispatchers.Default).launch {
            updateTimeseries()
            delay(60_000)
        }
    }

    suspend fun updateTimeseries() {
        baseFlows.values.map {
            scope.async {
                it.sync()
            }
        }.forEach { it.await() }
    }

    suspend fun getOhlcsForTf(ticker: InstrId, timeFrame: Interval): List<Ohlc> {
        return initTimeframeIfNeeded(ticker).getOhlcForTimeframe(timeFrame)
    }

    suspend fun prune(instrId: InstrId){
        log.info("pruning ${instrId}")
        baseFlows.get(instrId)?.reset()
        scope.launch {
            baseFlows.get(instrId)?.sync()
        }
        log.info("sync scheduled ${instrId}")
    }

    suspend fun initTimeframeIfNeeded(ticker: InstrId): SeriesContainer {
        var inited = false
        val persistFlow = baseFlows.computeIfAbsent(ticker, {
            inited = true
            SeriesContainer(
                daoProvider.getDao(ticker.sourceEnum(), Interval.Min10),
                sourceProvider[ticker.sourceEnum()], ticker
            )
        })
        if(inited){
            persistFlow.sync()
        }
        return persistFlow
    }
}

