package firelib.poligon

import com.fasterxml.jackson.databind.node.ArrayNode
import com.firelib.techbot.command.CacheService
import firelib.common.initDatabase
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.date
import firelib.core.misc.mapper
import firelib.core.misc.readJson
import firelib.core.store.MdStorageImpl
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class PoligonSource : HistoricalSource {




    override fun symbols(): List<InstrId> {
        val cachedSymbols = CacheService.getCached("poligon_symbols_v1", {
            val template = RestTemplate()
            val args = "active=true&apiKey=mBbK9N0OGVjrr6GrDMyz9N8Nxxnl2BVN&limit=1000"
            val url = "https://api.polygon.io/v3/reference/tickers?$args"
            var json = template.getForEntity(url, String::class.java).body.readJson()
            val arrayNode = json["results"] as ArrayNode

            while (json["next_url"] != null) {
                json = template.getForEntity(json["next_url"].textValue() + "&${args}", String::class.java).body.readJson()
                arrayNode.addAll((json["results"] as ArrayNode))
            }
            mapper.writeValueAsBytes(arrayNode)
        }, Interval.Day.durationMs * 5)

        return String(cachedSymbols).readJson().flatMap {
            try {
                val code = it["ticker"].textValue()
                val market = if (it["market"].textValue() == "stocks") it["primary_exchange"].textValue() else it["market"].textValue()
                listOf(InstrId(
                    code = code,
                    name = it["name"].textValue(),
                    id = "${code}_${market}",
                    market = market,
                    source = SourceName.POLIGON.name
                ))
            }catch (e : Exception){
                println("failed to parse ${it}")
                emptyList()
            }
        }
    }

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(600), Interval.Min10)

    }

    private fun fetchChunk(instrId: InstrId, from: LocalDate, to: LocalDate, interval: Interval): List<Ohlc> {
        val template = RestTemplate()
        val mins = interval.duration.toMinutes()
        val url =
            "https://api.polygon.io/v2/aggs/ticker/${instrId.code}/range/${mins}/minute/${from}/${to}?adjusted=true&sort=asc&limit=50000&apiKey=mBbK9N0OGVjrr6GrDMyz9N8Nxxnl2BVN"
        val startTime = System.nanoTime()
        val restStr = template.getForEntity(url, String::class.java).body

        val msTime = (System.nanoTime() - startTime) / 1000_000
        if(msTime > 1000){
            println("took $msTime ms")
        }
        val json = restStr.readJson()
        if(json["results"] == null){
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


    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {
        val chunkDays = 200L
        return sequence {
            var from = dateTime.toLocalDate()
            var to = dateTime.toLocalDate().plusDays(chunkDays)

            var filterBefore = Instant.EPOCH

            while (true) {
                val ohlcs = fetchChunk(instrId, from, to, interval).filter { it.endTime > filterBefore }
                println(ohlcs.size)
                if (ohlcs.isEmpty()) {
                    if(to.isBefore(LocalDate.now().minusDays(5))){
                        from = to
                        to = from.plusDays(chunkDays)
                    }else{
                        break
                    }
                }else{
                    yieldAll(ohlcs)
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

fun main() {

    initDatabase()


    PoligonSource().symbols().filter { it.code == "BR" }.forEach {
        println(it)
    }


}