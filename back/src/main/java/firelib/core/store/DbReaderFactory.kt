package firelib.core.store

import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.store.reader.SimplifiedReader
import firelib.finam.FinamDownloader
import java.time.Instant


class DbReaderFactory(val source : SourceName, val interval: Interval, val startTime : Instant, val market : String = FinamDownloader.SHARES_MARKET) :
    ReaderFactory {

    val mdStorage = MdStorageImpl()

    override fun makeReader(security: String): SimplifiedReader {

        //require(source == SourceName.FINAM, {"not supported source ${source}"})

        return SimplifiedReaderImpl(
            mdStorage,
            InstrId(code = security, market=market, source = source.name),
            startTime = startTime,
            interval
        )
    }
}