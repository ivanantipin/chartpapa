package firelib.finam

import com.opencsv.CSVParserBuilder
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.FinamTickerMapper
import firelib.core.misc.moscowZoneId
import firelib.core.store.MdStorageImpl
import io.netty.util.concurrent.DefaultThreadFactory
import org.apache.commons.io.IOUtils
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock

class FinamDownloader(val batchDays : Int = 100) : AutoCloseable, HistoricalSource {

    private val client = DefaultAsyncHttpClient(
            DefaultAsyncHttpClientConfig.Builder()
                    .setFollowRedirect(true)
                    .setKeepAlive(true)
                    .setThreadFactory(DefaultThreadFactory("download fac", true))
                    .setConnectionTtl(5000)
                    .setRequestTimeout(180000)
                    .setMaxRequestRetry(3)
                    .build()
    )

    @Volatile
    internal var lastFinamCall: Long = 0

    val log = LoggerFactory.getLogger(javaClass)


    override fun close() {
        try {
            client.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun symbols(): List<InstrId> {
        try {
            val ins = URL("https://www.finam.ru/cache/N72Hgd54/icharts/icharts.js").openStream()
            val lines : List<String> = IOUtils.readLines(ins, "cp1251") as List<String>

            val map = HashMap<String, Array<String>>()
            lines.forEach { l -> populate(l, map) }

            val names = map["varaEmitentNames"]
            val ids = map["varaEmitentIds"]
            val codes = map["varaEmitentCodes"]!!
            val markets = map["varaEmitentMarkets"]


            return codes.mapIndexed { i, code ->
                InstrId(
                    id = ids!![i],
                    name = names!![i],
                    market = markets!![i],
                    code = codes[i].replace("'", ""),
                    source = SOURCE.name
                )
            }

        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    val cache = ConcurrentHashMap<Pair<String,String>, InstrId>()

    fun fixInstr(instrId: InstrId): InstrId {
        if(instrId.id == "N/A"){
            if(cache.isEmpty()){
                cache.putAll(symbols().associateBy { it.code to it.market })
            }
            require(cache.containsKey(instrId.code to instrId.market), {"non exisitng ${instrId.id}"})
            return cache[instrId.code to instrId.market]!!
        }else{
            return instrId
        }
    }

    override fun load(instrIdSpec: InstrId, interval: Interval): Sequence<Ohlc> {
        return load(instrIdSpec, LocalDateTime.now().minusDays(3000), interval)
    }

    @Synchronized
    override fun load(instrIdIn: InstrId, start: LocalDateTime, interval: Interval): Sequence<Ohlc> {
        val instrId = fixInstr(instrIdIn)

        //log.info("loading data from ${start} for instrument ${instrId}")

        var mstart = start
        return sequence {
            while (mstart < LocalDateTime.now()) {
                val finish = mstart.plusDays(batchDays.toLong())
                yieldAll(loadSome(instrId, interval, mstart, finish))
                mstart = finish.minus(interval.duration)
            }
        }
    }

    @Synchronized
    private fun loadSome(
        instrId: InstrId,
        interval: Interval,
        start: LocalDateTime,
        finishI: LocalDateTime
    ): List<Ohlc> {

        while (System.currentTimeMillis() - lastFinamCall < 1100) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }

        val finish = if (finishI.isAfter(LocalDateTime.now())) LocalDateTime.now() else finishI;

        val params = listOf(
                "f" to "table",
                "e" to ".csv",
                "dtf" to "1",
                "tmf" to "3",
                "MSOR" to "1", // end of period
                "mstime" to "on",
                "mstimever" to "1",
                "sep" to "3",
                "sep2" to "1",
                // "at" to "1", header
                "p" to "${Period.forInterval(interval).id}",
                "em" to instrId.id,
                "market" to instrId.market,
                "df" to "${start.dayOfMonth}",
                "mf" to "${(start.monthValue - 1)}",
                "yf" to "${start.year}",
                "dt" to "${finish.dayOfMonth}",
                "mt" to "${(finish.monthValue - 1)}",
                "yt" to "${finish.year}",
                "code" to instrId.code,
                "cn" to instrId.code,
                "to" to "${finish.year}.${finish.monthValue}.${finish.dayOfMonth}",
                "from" to "${start.year}.${start.monthValue}.${start.dayOfMonth}"
        )

        val url = "http://export.finam.ru/table.csv?" + params.map { "${it.first}=${it.second}" }.joinToString(separator = "&")

        log.debug(url)
        return client.prepareGet(url).execute()
            .toCompletableFuture()
            .thenApply { response ->
                val lines = InputStreamReader(response.responseBodyAsStream, "cp1251").readLines()
                lastFinamCall = System.currentTimeMillis()
                lines.map { parseOhlc(it) }.filter { it != null }.map { it!! }
            }.get()
    }

    override fun getName(): SourceName {
        return SourceName.FINAM
    }

    enum class FinamMarket(val id: String) {
        SHARES_MARKET("1"),
        FUTURES_MARKET("14"),
        FX("5"),
        BATS("25");

        companion object {
            fun decode(id: String): FinamMarket? {
                return values().find { it.id == id }
            }
        }

    }

    companion object {
        private val log = LoggerFactory.getLogger(FinamDownloader::class.java)
        val SOURCE = SourceName.FINAM
        val SHARES_MARKET = "1"
        val ETF_MARKET = "517"
        val FUTURES_MARKET = "14"
        val BATS_MARKET = "25"
        val FX = "5"
        val FX_MARKET = "45"
    }

    val parser = CSVParserBuilder().withQuoteChar('\'').build()

    internal fun populate(inl: String, map: MutableMap<String, Array<String>>) {
        if (inl.indexOf('[') < 0) return
        val origStr = inl.substring(inl.indexOf('[') + 1, inl.indexOf(']'))
        var data = origStr.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
        val key = inl.substring(0, inl.indexOf('['))
                .replace(" ", "")
                .replace("=", "")

        if (key == "varaEmitentNames") {
            try {
                data = parser.parseLine(origStr)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        }
        map[key] = data
    }

    internal var pattern = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")

    fun parseOhlc(str: String): Ohlc? {
        return parseWithPattern(str, pattern)
    }

    fun parseWithPattern(str: String, pattern: DateTimeFormatter): Ohlc? {
        try {
            val arr = str.split(";")
            return Ohlc(LocalDateTime.parse(arr[0] + " " + arr[1], pattern).atZone(moscowZoneId).toInstant(),
                    arr[2].toDouble(),
                    arr[3].toDouble(),
                    arr[4].toDouble(),
                    arr[5].toDouble(),
                    volume = arr[6].toLong())
        } catch (e: Exception) {
            log.info("not valid entry " + str + " because " + e.message)
            return null
        }
    }

    val tickerMapper = FinamTickerMapper(this)

    override fun mapSecurity(security: String): InstrId {
        val tickerMapper1 = tickerMapper(security)
        require(tickerMapper1 != null, {"cant find symbol for ${security}"})
        return tickerMapper1
    }
}


fun main() {


    val ins = URL("https://www.finam.ru/cache/N72Hgd54/icharts/icharts.js").openStream()
    val lines : List<String> = IOUtils.readLines(ins, "utf-8") as List<String>
    lines.forEach { println(it) }

}

