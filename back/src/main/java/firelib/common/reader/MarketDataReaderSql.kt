package firelib.common.reader

import firelib.domain.Ohlc
import java.time.Instant

class MarketDataReaderSql(val ohlcs: List<Ohlc>) : MarketDataReader<Ohlc> {

    var cind = 0;

    override fun seek(time: Instant): Boolean {
        val idx = ohlcs.indexOfFirst { it.endTime.isAfter(time) }
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
        return ohlcs[0].endTime
    }

    override fun endTime(): Instant {
        return ohlcs.last().endTime
    }

    override fun close() {
    }
}