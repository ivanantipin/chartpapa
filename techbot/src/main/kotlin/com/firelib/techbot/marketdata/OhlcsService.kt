package com.firelib.techbot.marketdata

import com.firelib.techbot.InflightHandler
import com.firelib.techbot.persistence.BotConfig
import firelib.core.SourceName
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
        mapOf(SourceName.MOEX to 30*60_000L,SourceName.POLIGON to 5*60_000L).map { (source, delayMs)->
            CoroutineScope(Dispatchers.Default).launch {
                while (true){
                    try {
                        updateTimeseries(source)
                        delay(delayMs)
                    }catch (e : Exception){
                        log.error("failed to update timeseries, unexpected exception", e)
                    }
                }
            }
        }.forEach { job->
            job.invokeOnCompletion {
                println("TIMESERIES coroutine is OUT!!, err is ${it}")
                it?.printStackTrace()
            }
        }

    }

    suspend fun updateTimeseries(sourceName: SourceName) {
        log.info("updating ${baseFlows.size} instruments")

        baseFlows.values.filter { it.instrId.source == sourceName.name }.map {
            scope.async {
                try {
                    InflightHandler.registerInflight("OHLC_SYNC"){
                        it.sync()
                    }
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
        return baseFlows.computeIfAbsent(ticker, {
            val ret = SeriesContainer(
                daoProvider.getDao(ticker.sourceEnum(), Interval.Min10),
                sourceProvider[ticker.sourceEnum()], ticker
            )
            runBlocking {
                ret.sync()
            }
            ret
        })
    }
}

suspend fun main() {
    val service = OhlcsService(MdDaoContainer(), SourceFactory())
    val ticker = MoexSourceAsync().symbols().first { it.code.contains("SBER") }

    service.initTimeframeIfNeeded(ticker)
}