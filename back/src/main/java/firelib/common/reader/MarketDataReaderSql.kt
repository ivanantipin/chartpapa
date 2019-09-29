package firelib.common.reader

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.interval.Interval
import firelib.domain.Ohlc
import java.time.Instant

class MarketDataReaderSql(val ohlcs : List<Ohlc>) : MarketDataReader<Ohlc> {

    var cind = 0;

    override fun seek(time: Instant): Boolean {
        val idx = ohlcs.indexOfFirst { it.dtGmtEnd.isAfter(time) }
        cind = idx
        return idx > 0
    }

    override fun current(): Ohlc {
        return ohlcs[cind]
    }

    override fun read(): Boolean {
        return ++cind < ohlcs.size
    }

    override fun startTime(): Instant {
        return ohlcs[0].dtGmtEnd
    }

    override fun endTime(): Instant {
        return ohlcs[ohlcs.size - 1].dtGmtEnd
    }

    override fun close() {
    }

}

fun main() {
    //
    val dao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)
    val reader = MarketDataReaderDb(dao, "sber", Instant.now())
    while (reader.read()){
        println(reader.current())
    }
}



