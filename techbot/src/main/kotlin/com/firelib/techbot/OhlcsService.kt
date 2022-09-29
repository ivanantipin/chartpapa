package com.firelib.techbot

import com.firelib.techbot.persistence.BotConfig
import com.firelib.techbot.persistence.ConfigService
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.misc.SqlUtils
import firelib.core.misc.atUtc
import firelib.core.misc.toInstantDefault
import firelib.core.store.MdDao
import firelib.core.store.MdStorageImpl
import firelib.iqfeed.ContinousOhlcSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

class OhlcsService(
    val daoProvider: (source: SourceName, interval: Interval) -> MdDao,
    val sourceProvider: (source: SourceName) -> HistoricalSource, val window: Long = BotConfig.window
) {

    class TimeFramesContainer {
        val series = ConcurrentHashMap<Interval, ContinousOhlcSeries>()

        val initialized = CountDownLatch(1)

        fun waitForInitialization() {
            initialized.await()
        }

        fun markInitialized() {
            initialized.countDown()
        }
    }

    fun CoroutineScope.launchFlow(instrId: InstrId, container: TimeFramesContainer) {

        val source = sourceProvider(instrId.sourceEnum())

        val dao = daoProvider(instrId.sourceEnum(), Interval.Min10)

        val last = dao.queryLast(instrId)?.endTime ?: getStartTime(Interval.Week).toInstantDefault()

        var latestFetched = last.atUtc()

        launch {

            while (true) {
                val lload = source.load(instrId, latestFetched, Interval.Min10)
                    .filter { it.endTime > latestFetched.toInstantDefault() }
                    .chunked(5000)

                var empty = true

                lload.forEach { ohlcs ->
                    dao.insertOhlc(ohlcs, instrId)
                    container.series.forEach { k, v ->
                        v.add(ohlcs)
                    }
                    latestFetched = ohlcs.last().endTime.atUtc()
                    println("latest fetched ${latestFetched}")
                    empty = false
                }


                if (empty) {
                    container.markInitialized()
                    delay(60_000)
                }
            }
        }
    }

    fun getStartTime(timeFrame: Interval) : LocalDateTime{
        return LocalDateTime.now().minus(timeFrame.duration.multipliedBy(window))
    }


    val baseFlows = ConcurrentHashMap<InstrId, TimeFramesContainer>()

    val scope = CoroutineScope(Dispatchers.IO)

    fun initSeries(ticker: InstrId, timeFrame: Interval): ContinousOhlcSeries {
        val transformingSeries = ContinousOhlcSeries(timeFrame)

        val dao = daoProvider(ticker.sourceEnum(), Interval.Min10)

        val ohlcs = dao.queryAll(MdStorageImpl.makeTable(ticker), getStartTime(timeFrame))

        ohlcs.chunked(500).forEach {
            transformingSeries.add(it)
        }
        return transformingSeries

    }

    fun getOhlcsForTf(ticker: InstrId, timeFrame: Interval): List<Ohlc> {

        val persistFlow = baseFlows.computeIfAbsent(ticker, {
            val ret = TimeFramesContainer()
            scope.launchFlow(ticker, ret)
            ret
        })

        persistFlow.waitForInitialization()

        val series = persistFlow.series.computeIfAbsent(timeFrame, {
            initSeries(ticker, timeFrame)
        })

        val maxAllowedReminder = when (timeFrame) {
            Interval.Day, Interval.Week -> {
                0.25
            }
            else -> 0.001
        }

        val data = series.getDataSafe()

        val last = data.last()

        val reminder = (last.endTime.toEpochMilli() - series.lastFetchedTs) / timeFrame.durationMs
        val completeBar = reminder <= maxAllowedReminder
        return if (completeBar) {
            data
        } else {
            data.subList(0, data.size - 1)
        }
    }

    companion object {

        val instance = getDefault()

        fun getDefault(): OhlcsService {
            val storageImpl = MdStorageImpl()
            return OhlcsService(storageImpl.daos::getDao, { storageImpl.sources.get(it) })
        }
    }
}

suspend fun main() {
    initDatabase()
    ConfigService.initSystemVars()

    val hh = OhlcsService.instance.getOhlcsForTf(
        InstrId(
            "pbf",
            code = "PBF",
            source = SourceName.POLIGON.name,
            market = "XNAs"
        ), Interval.Min30
    )

    println(hh)

    return


    mutableListOf<Int>().add(0, 0)

    val ticker = "A${System.currentTimeMillis()}"

    val instr = InstrId(id = ticker, market = "XNAS", code = ticker, source = SourceName.POLIGON.name)

    val dsForFile = SqlUtils.getDsForFile("/tmp/test.db")

    val dao = MdDao(dsForFile)

    val historicalSource = object : HistoricalSource {
        override fun symbols(): List<InstrId> {
            return listOf(instr)
        }

        override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
            TODO("Not yet implemented")
        }

        override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {
            var dt = dateTime
            return sequence {
                if (dateTime < LocalDateTime.now()) {
                    repeat(10, {
                        yield(
                            Ohlc(
                                endTime = dt.toInstantDefault(),
                                open = 1.0, high = 2.0, low = 0.5, close = 1.0, volume = 2
                            )
                        )
                        dt = dt.plus(interval.duration)
                    })
                }
            }
        }

        override fun getName(): SourceName {
            return SourceName.DUMMY
        }
    }

    val mmService = OhlcsService({ a, b -> dao }, {
        historicalSource
    })

    val ohlcs = mmService.getOhlcsForTf(instr, Interval.Min30)

    println("ohlcs " + ohlcs)

}