package com.firelib.techbot.marketdata

import com.firelib.techbot.persistence.BotConfig
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.misc.timeSequence
import firelib.core.store.HistoricalSourceProvider
import firelib.core.store.MdDaoContainer
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

    val pool = Executors.newFixedThreadPool(5){
        Thread(it)
    }

    val baseFlows = ConcurrentHashMap<InstrId, SeriesContainer>()

    fun start(){
        Thread{
            updateTimeseries()
            timeSequence(Instant.now(), Interval.Min10).forEach { _ ->
                updateTimeseries()
            }
        }.start()
    }

    fun updateTimeseries() {
        baseFlows.values.map {
            pool.submit {
                try {
                    it.sync()
                }  catch (e : Exception){
                   log.error("failed to update ${it.instrId}", e)
                }
            }
        }.forEach { it.get() }
    }

    fun getOhlcsForTf(ticker: InstrId, timeFrame: Interval): List<Ohlc> {
        return initTimeframeIfNeeded(ticker).getOhlcForTimeframe(timeFrame)
    }

    fun prune(instrId: InstrId){
        log.info("pruning ${instrId}")
        baseFlows.get(instrId)?.reset()
        pool.submit{
            baseFlows.get(instrId)?.sync()
        }
        log.info("sync scheduled ${instrId}")
    }

    fun initTimeframeIfNeeded(ticker: InstrId): SeriesContainer {
        val persistFlow = baseFlows.computeIfAbsent(ticker, {
            val ret = SeriesContainer(
                daoProvider.getDao(ticker.sourceEnum(), Interval.Min10),
                sourceProvider[ticker.sourceEnum()], ticker
            )
            ret.sync()
            ret
        })
        return persistFlow
    }
}

