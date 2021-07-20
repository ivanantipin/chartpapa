package firelib.eodhist

import com.fasterxml.jackson.databind.node.ArrayNode
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.readJson
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

    override fun symbols(): List<InstrId> {
        val template = RestTemplate()
        val url = "https://eodhistoricaldata.com/api/exchange-symbol-list/US?api_token=5e81907493e611.25433232"

        val str = String(EodHistSource::class.java.getResourceAsStream("/eodsymbols.json").readAllBytes())

        //val restStr = template.getForEntity(url, String::class.java).body
        return str.readJson().filter {
            //(  it["Exchange"]?.textValue() == "NASDAQ" || it["Exchange"]?.textValue() == "NYSE") &&
                    it["code"] != null && it["MarketCapitalization"].longValue() > 4000_000_000
        }.map {
            InstrId(id=it["code"].textValue(), code = it["code"].textValue(), name = it["name"].textValue(), market = it["exchange_short_name"].textValue(),  source =SourceName.EODHIST.name )
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
                val url = "https://eodhistoricaldata.com/api/intraday/${instrId.code}.US?fmt=json&api_token=5e81907493e611.25433232&interval=1m&from=${dt}&to=${dtto}"
                println(url)

                val ms = measureTimeMillis {  template.getForEntity(url, String::class.java).body}


                println("took ${ms}")
                val restStr = template.getForEntity(url, String::class.java).body

                val json = restStr.readJson()
                if((json as ArrayNode).size() != 0){
                    val ohlcs = json.map {
                        Ohlc(
                            endTime = Instant.ofEpochSecond(it["timestamp"].longValue()),
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
    val storage = MdStorageImpl()
    storage.updateMarketData(InstrId(id="BP", code = "BP", source = SourceName.EODHIST.name), Interval.Min10)

}