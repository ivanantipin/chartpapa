package firelib.core.store

import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.misc.atNy
import firelib.core.misc.atUtc
import firelib.iqfeed.IntervalTransformer
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

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
        var ret = dao.queryAll(makeTable(instrId), startTime).toList()
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

    override fun read(instrId: InstrId, interval: Interval, start: LocalDateTime): List<Ohlc> {
        val dao = md.getDao(instrId.sourceEnum(), interval)
        return dao.queryAll(makeTable(instrId), start).toList()
    }

    fun makeTable(instrId: InstrId): String {
        return "${instrId.code}_${instrId.market}"
    }

    override fun insert(instrId: InstrId, interval: Interval, ohlcs: List<Ohlc>) {
        val dao = md.getDao(instrId.sourceEnum(), interval)
        dao.insertOhlc(ohlcs, makeTable(instrId))
    }

    fun updateMarketData(instrId: InstrId, interval: Interval): Instant {
        val source = sources[instrId.sourceEnum()]
        return updateMd(instrId, source, interval)
    }

    fun deleteSince(instrId: InstrId, interval: Interval, time: Instant) {
        try {
            log.info("removing ${instrId} from ${time}")
            val dao = md.getDao(instrId.sourceEnum(), interval)
            dao.deleteSince(makeTable(instrId), time)
        } catch (e: Exception) {
            log.info("failed to remove ${instrId} from ${time}", e)
        }
    }

    fun queryPoint(instrId: InstrId, interval: Interval, epochMs: Long): Ohlc? {
        val dao = md.getDao(instrId.sourceEnum(), interval)
        return dao.queryPoint(makeTable(instrId), epochMs)
    }

    fun lock(name : String, block : ()->Unit){
        val lock = GlobalConstants.lock(name)
        lock.lock()
        try {
            block()
        }finally {
            lock.unlock()
        }
    }


    private fun updateMd(instrId: InstrId, source: HistoricalSource, interval: Interval): Instant {
        val lockName = "${source.getName()}_${interval.name}"
        var instant = Instant.now()
        try {
            val dao = md.getDao(instrId.sourceEnum(), interval)
            val last = dao.queryLast(makeTable(instrId))
            val startTime = if (last == null) LocalDateTime.now().minusDays(5000) else last.endTime.atUtc().minus(interval.duration)

            source.load(instrId, startTime, interval).chunked(5000).forEach {
                instant = it.last().endTime
                lock(lockName){
                    dao.insertOhlc(it, makeTable(instrId))
                }
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
    val instrId = InstrId(code = "UVXY", source = SourceName.IQFEED.name)
    val date = LocalDate.of(2021, 1, 25)
    val read = impl.read(instrId, Interval.Min1, LocalDateTime.of(2021, 1, 25, 0, 0, 0))

    IntervalTransformer.transform(Interval.Min10, read.filter { it.endTime.atNy().toLocalDate() == date }).forEach {
        println("${it.endTime.minusSeconds(600).atNy()}   ->  ${it.close}")
    }
}
