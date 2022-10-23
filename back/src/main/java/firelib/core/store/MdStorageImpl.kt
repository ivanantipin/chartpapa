package firelib.core.store

import firelib.core.HistoricalSource
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.misc.atUtc
import firelib.iqfeed.IntervalTransformer
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.LocalDateTime

class MdStorageImpl(private val folder: String = GlobalConstants.mdFolder.toString()) : MdStorage {

    val log = LoggerFactory.getLogger(javaClass)

    val daos = MdDaoContainer(folder)

    val sources = SourceFactory()

    init {
        FileUtils.forceMkdir(File(folder))
    }

    companion object {
        fun makeTableName(instrId: InstrId): String {
            return "${instrId.code}_${instrId.market}"
        }
    }

    override fun read(instrId: InstrId, interval: Interval, start: LocalDateTime): List<Ohlc> {
        val dao = daos.getDao(instrId.sourceEnum(), interval)
        return dao.queryAll(makeTableName(instrId), start).toList()
    }

    fun insert(instrId: InstrId, interval: Interval, ohlcs: List<Ohlc>) {
        val dao = daos.getDao(instrId.sourceEnum(), interval)
        dao.insertOhlc(ohlcs, makeTableName(instrId))
    }

    fun updateMarketData(instrId: InstrId, interval: Interval): Instant {
        val source = sources[instrId.sourceEnum()]
        return updateMd(instrId, source, interval)
    }

    fun queryPoint(instrId: InstrId, interval: Interval, epochMs: Long): Ohlc? {
        val dao = daos.getDao(instrId.sourceEnum(), interval)
        return dao.queryPoint(makeTableName(instrId), epochMs)
    }

    private fun updateMd(instrId: InstrId, source: HistoricalSource, interval: Interval): Instant {
        var instant = Instant.now()
        try {
            val dao = daos.getDao(instrId.sourceEnum(), interval)
            val last = dao.queryLast(makeTableName(instrId))
            val startTime =
                if (last == null) LocalDateTime.now().minusDays(5000) else last.endTime.atUtc().minus(interval.duration)

            source.load(instrId, startTime, interval).chunked(5000).forEach {
                instant = it.last().endTime
                dao.insertOhlc(it, makeTableName(instrId))
            }
        } catch (e: Exception) {
            log.info("failed to update " + instrId + " " + e.message)
            e.printStackTrace()
        }
        return instant
    }

    fun transform(instrId: InstrId, from: Interval, to: Interval) {
        val dao = daos.getDao(instrId.sourceEnum(), from)
        val daoTo = daos.getDao(instrId.sourceEnum(), to)
        val toOhlc = IntervalTransformer.transform(to, dao.queryAll(instrId.code))
        daoTo.insertOhlc(toOhlc, dao.normName(instrId.code))
    }
}