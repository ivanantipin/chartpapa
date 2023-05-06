package com.firelib.techbot.marketdata

import com.firelib.techbot.persistence.BotConfig
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.store.HistoricalSourceProvider
import firelib.core.store.MdDaoContainer
import firelib.core.store.SourceFactory
import firelib.finam.MoexSourceAsync
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

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
        val job = CoroutineScope(Dispatchers.Default).launch {
            while (true){
                try {
                    updateTimeseries()
                    delay(60_000)
                }catch (e : Exception){
                    log.error("failed to update timeseries")
                }
            }
        }

        job.invokeOnCompletion {
            println("TIMESERIES coroutine is OUT!!, err is ${it}")
            if(it != null){
                it.printStackTrace()
            }
        }
    }

    suspend fun updateTimeseries() {
        log.info("updating ${baseFlows.size} instruments")
        baseFlows.values.map {
            scope.async {
                try {
                    it.sync()
                }catch (e : Exception){
                    log.error("Failed to sync ${it.instrId}", e)
                }
            }
        }.forEach {
            it.await()
        }
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

suspend fun main() {
    val service = OhlcsService(MdDaoContainer(), SourceFactory())
    val ticker = MoexSourceAsync().symbols().first { it.code.contains("SBER") }

    service.initTimeframeIfNeeded(ticker)
}