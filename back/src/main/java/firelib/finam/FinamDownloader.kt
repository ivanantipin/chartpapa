package firelib.finam

import com.google.common.io.CharStreams.readLines
import com.google.common.util.concurrent.SettableFuture
import com.opencsv.CSVParserBuilder
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.FinamTickerMapper
import firelib.core.misc.moscowZoneId
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


class FinamDownloader : AutoCloseable, HistoricalSource {

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


    override fun close() {
        try {
            client.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    override fun symbols(): List<InstrId> {
        try {
            val ins = URL("https://www.finam.ru/cache/icharts/icharts.js").openStream()
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

    override fun load(instrIdSpec: InstrId): Sequence<Ohlc> {
        return load(instrIdSpec, LocalDateTime.now().minusDays(3000))
    }

    @Synchronized
    override fun load(instrId: InstrId, start: LocalDateTime): Sequence<Ohlc> {
        var mstart = start
        return sequence {
            while (mstart < LocalDateTime.now()) {
                val finish = mstart.plusDays(1005)
                yieldAll(loadSome(instrId, mstart, finish))
                mstart = finish.minusDays(2)
            }
        }
    }

    private fun loadSome(instrId: InstrId, start: LocalDateTime, finishI: LocalDateTime): List<Ohlc> {
        while (System.currentTimeMillis() - lastFinamCall < 1100) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        val finish = if (finishI.isAfter(LocalDateTime.now())) LocalDateTime.now() else finishI;

        lastFinamCall = System.currentTimeMillis()

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
                "p" to "${Period.TEN_MINUTES.id}",
                "em" to "${instrId.id}",
                "market" to "${instrId.market}",
                "df" to "${start.dayOfMonth}",
                "mf" to "${(start.monthValue - 1)}",
                "yf" to "${start.year}",
                "dt" to "${finish.dayOfMonth}",
                "mt" to "${(finish.monthValue - 1)}",
                "yt" to "${finish.year}",
                "code" to "${instrId.code}",
                "cn" to "${instrId.code}",
                "to" to "${finish.year}.${finish.monthValue}.${finish.dayOfMonth}",
                "from" to "${start.year}.${start.monthValue}.${start.dayOfMonth}"
        )

        val url = "http://export.finam.ru/table.csv?" + params.map { "${it.first}=${it.second}" }.joinToString(separator = "&")


        val ret = SettableFuture.create<List<String>>()
        log.info(url)
        client.prepareGet(url).execute()
                .toCompletableFuture()
                .thenAccept { response ->
                    log.info("Status", response.statusCode)
                    try {
                        val lines = readLines(InputStreamReader(response.responseBodyAsStream, "cp1251"))
                        ret.set(lines)
                    } catch (e: IOException) {
                        log.error("Can't read data", e)
                    }
                }

        try {
            return ret.get().map { parseOhlc(it) }.filter { it != null }.map { it!! }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun getName(): SourceName {
        return SourceName.FINAM
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min10
    }

    companion object{
        private val log = LoggerFactory.getLogger(FinamDownloader::class.java)
        val SOURCE = SourceName.FINAM
        val SHARES_MARKET = "1"
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

