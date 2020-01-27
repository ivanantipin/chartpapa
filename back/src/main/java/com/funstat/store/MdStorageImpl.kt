package com.funstat.store


import com.funstat.GlobalConstants
import com.funstat.Pair
import com.funstat.domain.InstrId
import com.funstat.domain.sourceEnum
import com.funstat.finam.FinamDownloader
import com.funstat.iqfeed.IntervalTransformer
import com.funstat.iqfeed.IqFeedHistoricalSource
import com.funstat.tcs.HistoricalSourceEmulator
import com.funstat.tcs.TcsHistoricalSource
import com.funstat.tcs.getContext
import com.funstat.vantage.VSymbolDownloader
import com.funstat.vantage.VantageDownloader
import firelib.common.core.HistoricalSource
import firelib.common.core.SourceName
import firelib.common.interval.Interval
import firelib.common.misc.atUtc
import firelib.common.report.GeGeWriter
import firelib.common.report.SqlUtils
import firelib.domain.Ohlc
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MdStorageImpl(private val folder: String = GlobalConstants.mdFolder.toString()) : MdStorage {

    val container = SingletonsContainer()

    val requestedDao = GeGeWriter<InstrId>("requested", Paths.get("$folder/meta.db"), InstrId::class, listOf("code"))
    val symbolsDao = GeGeWriter<InstrId>("symbols", Paths.get("$folder/meta.db"), InstrId::class, listOf("code"))
    val pairs = GeGeWriter<Pair>("pairs", Paths.get("$folder/meta.db"), Pair::class, listOf("key"))


    init {
        FileUtils.forceMkdir(File(folder))
    }


    val sources  = mapOf(
            FinamDownloader.SOURCE to FinamDownloader(),
            SourceName.TCS to TcsHistoricalSource(getContext()),
            HistoricalSourceEmulator.SOURCE to HistoricalSourceEmulator(),
            VantageDownloader.SOURCE to VantageDownloader(),
            IqFeedHistoricalSource.SOURCE to IqFeedHistoricalSource(Paths.get("/ddisk/globaldatabase/1MIN/STK"))
    )

    private val executor = Executors.newScheduledThreadPool(1)


    fun getSourceDefaultInterval(source : SourceName) : Interval{
        return sources[source]!!.getDefaultInterval()
    }

    fun getDao(source: SourceName, interval: Interval): MdDao {
        return container.get("$source/${interval}") {
            val folder = this.folder + "/" + source + "/"
            FileUtils.forceMkdir(File(folder))
            MdDao(SqlUtils.getDsForFile("$folder$interval.db"))
        }
    }



    override fun read(instrId: InstrId, interval: Interval): List<Ohlc> {
        requestedDao.write(listOf(instrId))
        val dao = getDao(instrId.sourceEnum(), sources[instrId.sourceEnum()]!!.getDefaultInterval())
        val target = interval
        val startTime = LocalDateTime.now().minusSeconds(target.durationMs * 600 / 1000)
        var ret = dao.queryAll(instrId.code, startTime)

        if (ret.isEmpty()) {
            updateMarketData(instrId)
            ret = dao.queryAll(instrId.code, startTime)
        }
        val start = System.currentTimeMillis()
        try {
            return IntervalTransformer.transform(target, ret)
        } finally {
            println("transformed in " + (System.currentTimeMillis() - start) / 1000.0 + " s. " + ret.size + " min bars")
        }
    }

    override fun save(code: String, source: SourceName, interval: Interval, data: List<Ohlc>) {
        getDao(source, interval).insertOhlc(data, code)
    }

    override fun start() {
        executor.scheduleAtFixedRate({
            try {
                requestedDao.read().forEach { symbol -> updateMarketData(symbol) }
            } catch (e: Exception) {
                println("failed to ")
                e.printStackTrace()
            }
        }, 0, 10, TimeUnit.MINUTES)
    }

    override fun updateSymbolsMeta() {
        val lastUpdated = pairs.read().find { it.key ==  SYMBOLS_LAST_UPDATED}
        if (lastUpdated == null || System.currentTimeMillis() - java.lang.Long.parseLong(lastUpdated.value) > 24 * 3600000) {
            println("updating symbols as they are stale")
            symbolsDao.write(sources.values.flatMap { s -> s.symbols() }.filter { s ->
                s.market == "1"
                        || s.sourceEnum() == VantageDownloader.SOURCE
                        || s.sourceEnum() == IqFeedHistoricalSource.SOURCE
            })
            pairs.write(listOf(Pair(SYMBOLS_LAST_UPDATED, "" + System.currentTimeMillis())))
        } else {
            println("not updating symbols as last update was " + LocalDateTime.ofEpochSecond(java.lang.Long.parseLong(lastUpdated.value) / 1000, 0, ZoneOffset.UTC))
        }

        if (false) {
            //fixme
            updateVantage()
        }

    }

    private fun updateVantage() {
        val lastVantageUpdated = pairs.read().find { it.key ==  VANTAGE_LAST_UPDATED}
        if (lastVantageUpdated == null) {
            val exec = Executors.newSingleThreadExecutor()
            exec.submit {
                VSymbolDownloader.updateVantageSymbols()
                pairs.write(listOf(Pair(VANTAGE_LAST_UPDATED, "" + System.currentTimeMillis())))
            }
        }
    }


    override fun meta(): List<InstrId> {
        return symbolsDao.read()
    }

    override fun updateRequested(code: String) {
        requestedDao.read().filter { it.code == code }.forEach { symbol -> updateMarketData(symbol) }
    }


    fun updateMarketData(instrId: InstrId) : Instant {
        println("updating ${instrId}")

        var instant = Instant.now()
        try {
            val source = sources[instrId.sourceEnum()]!!
            val dao = getDao(instrId.sourceEnum(), source.getDefaultInterval())
            val startTime = dao.queryLast(instrId.code).map { oh -> oh.endTime.atUtc().minusDays(2) }.orElse(LocalDateTime.now().minusDays(3000))

            println("start time is ${startTime}")

            source.load(instrId, startTime).chunked(5000).forEach {
                instant = it.last().endTime
                dao.insertOhlc(it, instrId.code)
            }

        } catch (e: Exception) {
            println("failed to update " + instrId + " " + e.message)
            e.printStackTrace()
        }
        return instant
    }

    companion object {

        val SYMBOLS_LAST_UPDATED = "SYMBOLS_LAST_UPDATED"
        private val VANTAGE_LAST_UPDATED = "VANTAGE_LAST_UPDATED"


        @JvmStatic
        fun main(args: Array<String>) {
            val mdStorage = MdStorageImpl()
            mdStorage.updateSymbolsMeta()
            println(mdStorage.meta().filter { s -> s.sourceEnum() == FinamDownloader.SOURCE })

        }
    }
}
