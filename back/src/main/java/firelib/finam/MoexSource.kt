package firelib.finam

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import firelib.core.HistoricalSource
import firelib.core.HistoricalSourceAsync
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.atMoscow
import firelib.core.misc.toInstantMoscow
import firelib.poligon.PoligonSourceAsync
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

var pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
var timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

class MoexSource : HistoricalSource {

    val log = LoggerFactory.getLogger(javaClass)
    override fun symbols(): List<InstrId> {
        try {
            val request =
                "https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities.json?iss.meta=off&iss.only=securities"
            val template = RestTemplate()

            val entity = template.getForEntity(request, String::class.java)

            val mapper = ObjectMapper()
            val obj: JsonNode = mapper.readTree(entity.body!!)

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

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(10000), interval)
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {

        val template = RestTemplate()
        val url =
            "http://iss.moex.com/iss/engines/stock/markets/shares/boards/${instrId.board}/securities/${instrId.code}/candles.json"

        val ret = mutableListOf<Ohlc>()

        val mapper = ObjectMapper()
        var curDt = dateTime.toLocalDate()
        var prevFetched = Instant.MIN

        while (curDt < LocalDate.now().plusDays(1)) {

            val from = pattern.format(curDt)

            val entity = template.getForEntity("${url}?from=${from}&interval=10&start=0", String::class.java)

            val obj: JsonNode = mapper.readTree(entity.body)

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

            ret += rr

            prevFetched = rr.last().endTime

            println("size is ${ret.size} last ${ret.last()}")

        }
        return sequence {
            yieldAll(ret)
        }
    }

    override fun getName(): SourceName {
        return SourceName.MOEX
    }

    val secCache : Map<String, InstrId> by lazy {
        symbols().associateBy { it.code.lowercase() }
    }

    override fun getAsyncInterface(): HistoricalSourceAsync {
        return MoexSourceAsync()
    }

    override fun mapSecurity(security: String): InstrId {
        return secCache.getOrDefault(security.lowercase(), InstrId())
    }

}

suspend fun main() {
    PoligonSourceAsync("mBbK9N0OGVjrr6GrDMyz9N8Nxxnl2BVN").symbols().forEach {
        println(it)
    }
}