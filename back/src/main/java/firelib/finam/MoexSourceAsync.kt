package firelib.finam

import firelib.core.HistoricalSourceAsync
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.atMoscow
import firelib.core.misc.readJson
import firelib.core.misc.toInstantMoscow
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class MoexSourceAsync : HistoricalSourceAsync {

    val log = LoggerFactory.getLogger(javaClass)

    val client = HttpClient()


    override suspend fun symbols(): List<InstrId> {
        try {
            val request =
                "https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities.json?iss.meta=off&iss.only=securities"


            val obj = client.get(request).bodyAsText().readJson()

            val header = obj["securities"]["columns"].mapIndexed { idx, hh -> hh.asText()!! to idx }.toMap()

            //"SECID", "BOARDID", "SHORTNAME", "PREVPRICE", "LOTSIZE"
            return obj["securities"]["data"].map {
                InstrId(
                    id = it[header["SECID"]!!].asText() + "_MOEX",
                    code = it[header["SECID"]!!].asText(),
                    lot = it[header["LOTSIZE"]!!].asInt(),
                    board = it[header["BOARDID"]!!].asText(),
                    minPriceIncr = BigDecimal.ONE.divide(
                        BigDecimal(
                            Math.pow(
                                10.0,
                                it[header["DECIMALS"]!!].asText().toDouble()
                            )
                        )
                    ),
                    name = it[header["SHORTNAME"]!!].asText(),
                    source = SourceName.MOEX.name,
                    market = "MOEX"
                )
            }
        } catch (e: Exception) {
            log.error("error happened", e)
            return emptyList()
        }
    }

    override suspend fun load(instrId: InstrId, interval: Interval): Flow<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(10000), interval)
    }

    override suspend fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Flow<Ohlc> {

        val url =
            "http://iss.moex.com/iss/engines/stock/markets/shares/boards/${instrId.board}/securities/${instrId.code}/candles.json"

        var curDt = dateTime.toLocalDate()
        var prevFetched = Instant.MIN

        return flow {
            while (curDt < LocalDate.now().plusDays(1)) {

                val from = pattern.format(curDt)

                val obj = client.get("${url}?from=${from}&interval=10&start=0").bodyAsText().readJson()

                val indexMap =
                    obj["candles"]["columns"].toList().mapIndexed({ idx, nd -> Pair(nd.textValue(), idx) }).toMap()
                val openIdx = indexMap["open"]!!.toInt()
                val closeIdx = indexMap["close"]!!.toInt()
                val highIdx = indexMap["high"]!!.toInt()
                val lowIdx = indexMap["low"]!!.toInt()
                val volumeIdx = indexMap["volume"]!!.toInt()
                val beginIdx = indexMap["begin"]!!.toInt()

                val rr = obj["candles"]["data"].map {
                    Ohlc(
                        open = it[openIdx].doubleValue(),
                        high = it[highIdx].doubleValue(),
                        low = it[lowIdx].doubleValue(),
                        close = it[closeIdx].doubleValue(),
                        volume = it[volumeIdx].longValue(),
                        endTime = LocalDateTime.parse(it[beginIdx].asText(), timeFormatter).toInstantMoscow()
                            .plusSeconds(600),
                        interpolated = false
                    )
                }.filter { it.endTime > prevFetched }

                if (rr.isEmpty()) {
                    break
                }

                curDt = rr.last().endTime.atMoscow().toLocalDate()


                prevFetched = rr.last().endTime

                println("fetched size ${rr.size} last ${rr.last()}")

                rr.forEach {
                    emit(it)
                }
            }
        }
    }

    override fun getName(): SourceName {
        return SourceName.MOEX
    }


}