package firelib.core.store

import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.store.reader.SimplifiedReader
import firelib.finam.FinamDownloader
import java.time.Instant


class DbReaderFactory(val source : SourceName, val interval: Interval, val startTime : Instant) :
    ReaderFactory {

    val mdStorage = MdStorageImpl()

    override fun makeReader(security: String): SimplifiedReader {

        require(source == SourceName.FINAM, {"not supported source ${source}"})

        // fixme hack
        val instrId = InstrId(code = security, market = FinamDownloader.SHARES_MARKET)

        return SimplifiedReaderImpl(
            mdStorage,
            instrId,
            startTime = startTime,
            interval
        )
    }
}