package firelib.eodhist

import com.fasterxml.jackson.databind.node.ArrayNode
import com.firelib.techbot.command.CacheService
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.readJson
import firelib.core.report.initDatabase
import firelib.core.store.MdStorageImpl
import firelib.iqfeed.IntervalTransformer
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.system.measureTimeMillis

class EodHistSource : HistoricalSource {

    fun mdExists(instrId: InstrId) : Boolean{
        return load(instrId, LocalDateTime.now().minusDays(5), Interval.Min1).count() > 0
    }


    override fun  symbols(): List<InstrId> {
        val cachedSymbols = CacheService.getCached("eodhist_symbols", {
            val template = RestTemplate()
            val url = "https://eodhistoricaldata.com/api/exchange-symbol-list/US?api_token=5e81907493e611.25433232&fmt=json"
            template.getForEntity(url, String::class.java).body.toByteArray()
        }, 5*Interval.Week.durationMs)

        return String(cachedSymbols).readJson().filter {
            it["Exchange"]?.textValue() == "NASDAQ" || it["Exchange"]?.textValue() == "NYSE" && it["Code"] != null
                    && it["Type"]?.textValue() == "Common Stock"
        }.map {
            InstrId(id=it["Code"].textValue(), code = it["Code"].textValue(), name = it["Name"].textValue(), market = it["Exchange"].textValue(),  source =SourceName.EODHIST.name )
        }
    }

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        return load(instrId, Interval.Min5)

    }

    fun getNextTime(epochSec : Long) : Long{
        val nextTime = epochSec + 99*Interval.Day.duration.seconds
        return Interval.Day.truncTime(nextTime * 1000)/1000
    }


    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {

        if(dateTime.isBefore(LocalDateTime.now().minusDays(100)) &&
                    !mdExists(instrId)
        ){
            println("no market data for ${instrId}")
            return emptySequence()
        }

        val template = RestTemplate()

        var dt = dateTime.toEpochSecond(ZoneOffset.UTC)
        var dtto = getNextTime(dt)

        return sequence {
            while (true){

                println("from dt ${LocalDateTime.ofEpochSecond(dt, 0, ZoneOffset.UTC)}")
                println("to dt ${LocalDateTime.ofEpochSecond(dtto, 0, ZoneOffset.UTC)}")

                val url = "https://eodhistoricaldata.com/api/intraday/${instrId.code}.US?fmt=json&api_token=5e81907493e611.25433232&interval=1m&from=${dt}&to=${dtto}"

                println(url)

                val ms = measureTimeMillis {  template.getForEntity(url, String::class.java).body}

                println("took ${ms} to fetch")

                val restStr = template.getForEntity(url, String::class.java).body

                val json = restStr.readJson()

                println("size is ${json.size()}")

                if((json as ArrayNode).size() != 0){
                    val ohlcs = json.map {
                        Ohlc(
                            endTime = Instant.ofEpochSecond(it["timestamp"].longValue() + 60),
                            open = it["open"].doubleValue(),
                            high = it["high"].doubleValue(),
                            low = it["low"].doubleValue(),
                            close = it["close"].doubleValue(),
                            volume = it["volume"].longValue(),
                            interpolated = false
                        )
                    }
                    yieldAll(IntervalTransformer.transform(interval,  ohlcs).asSequence())
                    dt = json.last()["timestamp"].longValue() + 1
                    dtto = getNextTime(dt)
                }else{
                    if(dtto < Instant.now().epochSecond - 1000){
                        dt = dtto
                        dtto = getNextTime(dt)
                    }else{
                        break
                    }
                }
            }
        }
    }

    override fun getName(): SourceName {
        return SourceName.EODHIST
    }
}

fun main() {
    initDatabase()
    //println(EodHistSource().symbols().size)
    //return

    val storage = MdStorageImpl()
    storage.updateMarketData(InstrId(id="XLE", code = "XLE", source = SourceName.EODHIST.name), Interval.Min10)

}