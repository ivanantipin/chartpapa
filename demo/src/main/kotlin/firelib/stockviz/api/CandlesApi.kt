package firelib.stockviz.api

import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.core.store.MdStorageImpl
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.time.Instant

@Controller("/api/v1")
class CandlesApiController {

    @Get(
        value = "/candles/{timeframe}/{symbol}/",
        produces = ["application/json"]
    )
    fun candlesRead(symbol: String, timeframe: String, fromTs: Long, toTs: Long
    ): List<Candle> {

        val storage = MdStorageImpl()
        val dao = storage.md.getDao(SourceName.FINAM, Interval.Min10)
        val ohlcs = dao.queryAll(symbol.replace(".MICEX", ""), Instant.ofEpochMilli(fromTs).atMoscow())

        return ohlcs.map {
            Candle(
                it.endTime.toEpochMilli(),
                it.open.toBigDecimal(),
                it.high.toBigDecimal(),
                it.low.toBigDecimal(),
                it.close.toBigDecimal(),
                it.volume.toInt()
            )
        }.toList()
    }
}
