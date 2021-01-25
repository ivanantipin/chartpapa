package firelib.core.store

import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.misc.atUtc
import firelib.emulator.HistoricalSourceEmulator
import firelib.finam.FinamDownloader
import firelib.finam.MoexSource
import firelib.iqfeed.IntervalTransformer
import firelib.iqfeed.IqFeedHistoricalSource
import firelib.mt5.MT5SourceSafe
import firelib.vantage.VantageDownloader
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap


class SourceFactory{
    val sources = mapOf<SourceName, ()->HistoricalSource>(
        SourceName.FINAM to {FinamDownloader()},
        SourceName.VANTAGE to {VantageDownloader()},
        SourceName.DUMMY to { HistoricalSourceEmulator() },
        SourceName.MOEX to { MoexSource() },
        SourceName.IQFEED to {IqFeedHistoricalSource(Paths.get("/ddisk/globaldatabase/1MIN/STK"))},
        SourceName.MT5 to { MT5SourceSafe() }
    )

    val concurrentHashMap = ConcurrentHashMap<SourceName, HistoricalSource>()

    operator fun get(source: SourceName) : HistoricalSource{
        return concurrentHashMap.computeIfAbsent(source, {sources[source]!!()})
    }
}


class MdStorageImpl(private val folder: String = GlobalConstants.mdFolder.toString()) : MdStorage {

    val log = LoggerFactory.getLogger(javaClass)

    val md = MdDaoContainer()

    val sources = SourceFactory()

    init {
        FileUtils.forceMkdir(File(folder))
    }

    override fun read(instrId: InstrId, interval: Interval, targetInterval: Interval): List<Ohlc> {
        val dao = md.getDao(instrId.sourceEnum(), interval)
        val startTime = LocalDateTime.now().minusSeconds(targetInterval.durationMs * 600 / 1000)
        var ret = dao.queryAll(instrId.code, startTime)
        if (ret.isEmpty()) {
            updateMarketData(instrId, interval)
            ret = dao.queryAll(instrId.code, startTime).toList()
        }
        val start = System.currentTimeMillis()
        try {
            return IntervalTransformer.transform(targetInterval, ret)
        } finally {
            log.info("transformed in " + (System.currentTimeMillis() - start) / 1000.0 + " s. " + ret.size + " min bars")
        }
    }

    fun updateMarketData(instrId: InstrId, interval: Interval): Instant {
        val source = sources[instrId.sourceEnum()]
        return updateMd(instrId, source, interval)
    }

    fun updateMd(instrId: InstrId, source: HistoricalSource, interval: Interval): Instant {
        var instant = Instant.now()
        try {
            val dao = md.getDao(instrId.sourceEnum(), interval)
            val last = dao.queryLast(makeTable(instrId))
            val startTime = if (last == null) LocalDateTime.now().minusDays(5000) else last.endTime.atUtc().minus(interval.duration)

            source.load(instrId, startTime, interval).chunked(5000).forEach {
                println("inserted")
                instant = it.last().endTime
                dao.insertOhlc(it, instrId.code)
            }
        } catch (e: Exception) {
            log.info("failed to update " + instrId + " " + e.message)
            e.printStackTrace()
        }
        return instant
    }

    fun transform(instrId: InstrId, from: Interval, to: Interval) {
        val dao = md.getDao(instrId.sourceEnum(), from)
        val daoTo = md.getDao(instrId.sourceEnum(), to)
        val toOhlc = IntervalTransformer.transform(to, dao.queryAll(instrId.code))
        daoTo.insertOhlc(toOhlc, dao.normName(instrId.code))
    }
}

fun main() {
    val impl = MdStorageImpl()
    impl.updateMarketData(InstrId(code = "GOOG", market =  "25", source = SourceName.FINAM.name), Interval.Min10)
}
