package firelib.core.store

import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.store.reader.SimplifiedReader
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