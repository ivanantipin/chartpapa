package firelib.poligon

import com.fasterxml.jackson.databind.node.ArrayNode
import com.firelib.techbot.command.CacheService
import firelib.core.HistoricalSourceAsync
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.date
import firelib.core.misc.mapper
import firelib.core.misc.readJson
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class PoligonSourceAsync(val token: String) : HistoricalSourceAsync{

    val client = HttpClient()

    val log = LoggerFactory.getLogger(javaClass)

    override suspend fun symbols(): List<InstrId> {

        val cachedSymbols = CacheService.getCached("poligon_symbols_v2", {
            runBlocking { fetchSymbols() }
        }, Interval.Day.durationMs*5)

        return String(cachedSymbols).readJson().map {
            try {
                val code = it["ticker"].textValue()
                val market = if (it["market"].textValue() == "stocks") it["primary_exchange"].textValue() else it["market"].textValue()
                InstrId(
                    code = code,
                    name = it["name"].textValue(),
                    id = "${code}_${market}",
                    market = market,
                    source = SourceName.POLIGON.name
                )
            } catch (e: Exception) {
                println("failed to parse ${it}")
                null
            }
        }.filterNotNull()
    }

     suspend fun fetchSymbols(): ByteArray {
         var json = client.get<String>("https://api.polygon.io/v3/reference/tickers"){
             url {
                 parameters.append("active", "true")
                 parameters.append("apiKey", token)
                 parameters.append("limit", "1000")
             }
         }.readJson()

        val arrayNode = json["results"] as ArrayNode

        while (json["next_url"] != null) {
            json = client.get<String>(json["next_url"].textValue()){
                log.info("fetching symbols, current size is ${arrayNode.size()}")
                url {
                    parameters.append("apiKey", token)
                }
            }.readJson()
            arrayNode.addAll((json["results"] as ArrayNode))
        }

        return mapper.writeValueAsBytes(arrayNode)
    }

    override suspend fun load(instrId: InstrId, interval: Interval): Flow<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(600), Interval.Min10)
    }

    suspend fun fetchChunk(instrId: InstrId, from: LocalDate, to: LocalDate, interval: Interval): List<Ohlc> {

        val mins = interval.duration.toMinutes()
        val startTime = System.nanoTime()


        val msTime = (System.nanoTime() - startTime) / 1000_000
        if (msTime > 1000) {
            println("took $msTime ms to fetch ${instrId.code}")
        }

        val json = client.get<String>("https://api.polygon.io/v2/aggs/ticker/${instrId.code}/range/${mins}/minute/${from}/${to}"){
            url {
                parameters.append("apiKey", token)
                parameters.append("adjusted", "true")
                parameters.append("sort", "asc")
                parameters.append("limit", "5000")
            }
        }.readJson()

        if (json["results"] == null) {
            return emptyList()
        }
        return (json["results"] as ArrayNode).map {
            Ohlc(
                endTime = Instant.ofEpochMilli(it["t"].longValue() + interval.durationMs),
                open = it["o"].doubleValue(),
                high = it["h"].doubleValue(),
                low = it["l"].doubleValue(),
                close = it["c"].doubleValue(),
                volume = it["v"].longValue(),
                interpolated = false
            )
        }

    }

    override suspend fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Flow<Ohlc> {
        val chunkDays = 200L
        return flow {
            var from = dateTime.toLocalDate()
            var to = dateTime.toLocalDate().plusDays(chunkDays)

            var filterBefore = Instant.EPOCH

            while (true) {
                val ohlcs = fetchChunk(instrId, from, to, interval).filter { it.endTime > filterBefore }
                if (ohlcs.isEmpty()) {
                    if (to.isBefore(LocalDate.now().minusDays(5))) {
                        from = to
                        to = from.plusDays(chunkDays)
                    } else {
                        break
                    }
                } else {
                    ohlcs.forEach {
                        emit(it)
                    }
                    from = ohlcs.last().date()
                    to = from.plusDays(chunkDays)
                    filterBefore = ohlcs.last().endTime
                }
            }
        }
    }

    override fun getName(): SourceName {
        return SourceName.POLIGON
    }
}
