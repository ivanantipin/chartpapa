package firelib.store

import firelib.common.core.ReaderFactory
import firelib.common.core.SourceName
import firelib.common.interval.Interval
import firelib.common.reader.SimplifiedReader
import java.time.Instant

//fixme time in make reader
class DbReaderFactory(source : SourceName, interval: Interval, val startTime : Instant) :
    ReaderFactory {

    val dao = MdDaoContainer().getDao(source, interval)

    override fun makeReader(security: String): SimplifiedReader {
        return SimplifiedReaderImpl(
            dao,
            security,
            startTime = startTime
        )
    }
}