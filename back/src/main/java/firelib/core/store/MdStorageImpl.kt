package firelib.core.store


import firelib.core.domain.InstrId
import firelib.core.domain.sourceEnum
import firelib.finam.FinamDownloader
import firelib.iqfeed.IntervalTransformer
import firelib.iqfeed.IqFeedHistoricalSource
import firelib.emulator.HistoricalSourceEmulator
import firelib.vantage.VantageDownloader
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.misc.atUtc
import firelib.core.report.dao.GeGeWriter
import firelib.core.misc.SqlUtils
import firelib.core.domain.Ohlc
import firelib.core.misc.Mt5CsvSource
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap


class MdDaoContainer(val folder: String = GlobalConstants.mdFolder.toString()) {
    val container = SingletonsContainer()

    fun getDao(source: SourceName, interval: Interval): MdDao {
        return container.get("$source/${interval}") {
            val folder = this.folder + "/" + source + "/"
            FileUtils.forceMkdir(File(folder))
            MdDao(SqlUtils.getDsForFile("$folder$interval.db"))
        }
    }
}


class SourceFactory{
    val sources = mapOf<SourceName, ()->HistoricalSource>(
        SourceName.FINAM to {FinamDownloader()},
        SourceName.VANTAGE to {VantageDownloader()},
        SourceName.DUMMY to { HistoricalSourceEmulator(Interval.Sec10) },
        SourceName.IQFEED to {IqFeedHistoricalSource(Paths.get("/ddisk/globaldatabase/1MIN/STK"))},
        SourceName.MT5 to { Mt5CsvSource() }
    )

    val concurrentHashMap = ConcurrentHashMap<SourceName, HistoricalSource>()

    operator fun get(source: SourceName) : HistoricalSource{
        return concurrentHashMap.computeIfAbsent(source, {sources[source]!!()})
    }

}


class MdStorageImpl(private val folder: String = GlobalConstants.mdFolder.toString()) : MdStorage {

    val log = LoggerFactory.getLogger(javaClass)

    val requestedDao = GeGeWriter<InstrId>(
        Paths.get("$folder/meta.db"),
        InstrId::class,
        listOf("code"),
        "requested"
    )
    val symbolsDao = GeGeWriter<InstrId>(
        Paths.get("$folder/meta.db"),
        InstrId::class,
        listOf("code"),
        "symbols"
    )

    val md = MdDaoContainer()

    val sources = SourceFactory()

    init {
        FileUtils.forceMkdir(File(folder))
    }

    fun getSourceDefaultInterval(source: SourceName): Interval {
        return sources[source].getDefaultInterval()
    }


    override fun read(instrId: InstrId, interval: Interval): List<Ohlc> {
        requestedDao.write(listOf(instrId))
        val dao = md.getDao(instrId.sourceEnum(), sources[instrId.sourceEnum()].getDefaultInterval())
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
            log.info("transformed in " + (System.currentTimeMillis() - start) / 1000.0 + " s. " + ret.size + " min bars")
        }
    }

    override fun save(code: String, source: SourceName, interval: Interval, data: List<Ohlc>) {
        md.getDao(source, interval).insertOhlc(data, code)
    }

    override fun meta(): List<InstrId> {
        return symbolsDao.read()
    }

    override fun updateRequested(code: String) {
        requestedDao.read().filter { it.code == code }.forEach { symbol -> updateMarketData(symbol) }
    }


    fun updateMarketData(instrId: InstrId): Instant {
        val source = sources[instrId.sourceEnum()]
        return updateMd(instrId, source)
    }

    fun updateMd(instrId: InstrId, source: HistoricalSource): Instant {
        var instant = Instant.now()
        try {
            val dao = md.getDao(instrId.sourceEnum(), source.getDefaultInterval())
            val startTime = dao.queryLast(instrId.code).map { oh -> oh.endTime.atUtc().minusDays(2) }
                .orElse(LocalDateTime.now().minusDays(3000))

            log.info("start time is ${startTime}")

            source.load(instrId, startTime).chunked(5000).forEach {
                instant = it.last().endTime
                dao.insertOhlc(it, instrId.code)
            }
        } catch (e: Exception) {
            log.info("failed to update " + instrId + " " + e.message)
            e.printStackTrace()
        }
        return instant
    }

    fun transform(instrId: InstrId, from : Interval, to : Interval){
        val dao = md.getDao(instrId.sourceEnum(), from)
        val daoTo = md.getDao(instrId.sourceEnum(), to)
        val toOhlc = IntervalTransformer.transform(to, dao.queryAll(instrId.code))
        daoTo.insertOhlc(toOhlc, dao.normName(instrId.code))
    }
}

