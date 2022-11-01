package firelib.finam

import com.opencsv.CSVParserBuilder
import firelib.core.HistoricalSourceAsync
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.FinamTickerMapper
import firelib.core.misc.moscowZoneId
import io.netty.util.concurrent.DefaultThreadFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FinamDownloaderAsync(val batchDays: Int = 100) : AutoCloseable, HistoricalSourceAsync {

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

    override suspend fun symbols(): List<InstrId> {
        return suspendCoroutine { cont ->
            client.prepareGet("https://www.finam.ru/cache/N72Hgd54/icharts/icharts.js").execute().toCompletableFuture()
                .handle({ resp, thr ->

                    if (thr != null) {
                        cont.resumeWithException(thr)
                    } else {
                        try {
                            val map = mutableMapOf<String, Array<String>>()
                            InputStreamReader(resp.responseBodyAsStream, "cp1251")
                                .readLines()
                                .forEach { l -> populate(l, map) }

                            val result = map["varaEmitentCodes"]!!.mapIndexed { i, code ->
                                InstrId(
                                    id = map["varaEmitentIds"]!![i],
                                    name = map["varaEmitentNames"]!![i],
                                    market = map["varaEmitentMarkets"]!![i],
                                    code = map["varaEmitentCodes"]!![i].replace("'", ""),
                                    source = SOURCE.name
                                )
                            }.filter {instr-> FinamDownloader.FinamMarket.values().any { it.id == instr.market } }
                            cont.resumeWith(Result.success(result))
                        }catch (e : Exception){
                            cont.resumeWithException(e)
                        }

                    }
                })
        }
    }

    val cache = ConcurrentHashMap<Pair<String, String>, InstrId>()

    suspend fun fixInstr(instrId: InstrId): InstrId {
        if (instrId.id == "N/A") {
            if (cache.isEmpty()) {
                cache.putAll(symbols().associateBy { it.code to it.market })
            }
            require(cache.containsKey(instrId.code to instrId.market), { "non exisitng ${instrId.id}" })
            return cache[instrId.code to instrId.market]!!
        } else {
            return instrId
        }
    }

    override suspend fun load(instrIdSpec: InstrId, interval: Interval): Flow<Ohlc> {
        return load(instrIdSpec, LocalDateTime.now().minusDays(3000), interval)
    }

    override suspend fun load(instrIdIn: InstrId, start: LocalDateTime, interval: Interval): Flow<Ohlc> {
        val instrId = fixInstr(instrIdIn)
        var mstart = start
        return flow {
            while (mstart < LocalDateTime.now()) {
                val finish = mstart.plusDays(batchDays.toLong())
                loadSome(instrId, interval, mstart, finish).forEach { emit(it) }
                mstart = finish.minus(interval.duration)
            }
        }
    }

    suspend fun loadSome(
        instrId: InstrId,
        interval: Interval,
        start: LocalDateTime,
        finishI: LocalDateTime
    ): List<Ohlc> {

        while (System.currentTimeMillis() - lastFinamCall < 1100) {
            delay(100)
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

        val url = "http://export.finam.ru/table.csv?" + params.map { "${it.first}=${it.second}" }
            .joinToString(separator = "&")


        return suspendCoroutine { cor ->
            client.prepareGet(url).execute()
                .toCompletableFuture()
                .handle { response, thr ->
                    if (thr != null) {
                        cor.resumeWithException(thr)
                    } else {
                        try {
                            val lines = InputStreamReader(response.responseBodyAsStream, "cp1251").readLines()
                            lastFinamCall = System.currentTimeMillis()
                            val data: List<Ohlc> = lines.map { parseOhlc(it) }.filter { it != null }.map { it!! }
                            cor.resumeWith(Result.success(data))
                        }catch (e : Exception){
                            cor.resumeWithException(e)
                        }
                    }
                }
        }

    }

    override fun getName(): SourceName {
        return SourceName.FINAM
    }


    companion object {
        private val log = LoggerFactory.getLogger(FinamDownloader::class.java)
        val SOURCE = SourceName.FINAM
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
            return Ohlc(
                LocalDateTime.parse(arr[0] + " " + arr[1], pattern).atZone(moscowZoneId).toInstant(),
                arr[2].toDouble(),
                arr[3].toDouble(),
                arr[4].toDouble(),
                arr[5].toDouble(),
                volume = arr[6].toLong()
            )
        } catch (e: Exception) {
            log.info("not valid entry " + str + " because " + e.message)
            return null
        }
    }
}

suspend fun main() {
    FinamDownloaderAsync().symbols().forEach { println(it) }
}

