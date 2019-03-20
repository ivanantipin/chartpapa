package com.funstat.finam

import com.funstat.domain.InstrId
import firelib.domain.Ohlc
import com.funstat.vantage.Source
import com.google.common.util.concurrent.SettableFuture
import com.opencsv.CSVParserBuilder
import firelib.common.interval.Interval
import org.apache.commons.io.IOUtils
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.slf4j.LoggerFactory

import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.HashMap

import com.google.common.io.CharStreams.readLines


class FinamDownloader : AutoCloseable, Source {

    private val client = DefaultAsyncHttpClient(
            DefaultAsyncHttpClientConfig.Builder()
                    .setFollowRedirect(true)
                    .setKeepAlive(true)
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
            val lines = IOUtils.readLines(ins, Charset.forName("cp1251"))

            val map = HashMap<String, Array<String>>()
            lines.forEach { l -> populate(l, map) }

            val names = map["varaEmitentNames"]
            val ids = map["varaEmitentIds"]
            val codes = map["varaEmitentCodes"]!!
            val markets = map["varaEmitentMarkets"]


            return codes.mapIndexed({i,code->
                InstrId(id= ids!![i], name= names!![i], market =  markets!![i], code=codes[i].replace("'", ""), source = SOURCE)
            })

        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    override fun load(instrIdSpec: InstrId): List<Ohlc> {
        return load(instrIdSpec, LocalDateTime.now().minusDays(3000))
    }

    @Synchronized
    override fun load(instrId: InstrId, start: LocalDateTime): List<Ohlc> {
        val ret = MutableList(0,{Ohlc()})
        var mstart = start
        while (mstart.isBefore(LocalDateTime.now())){
            val finish = mstart.plusDays(1005)
            ret += loadSome(instrId,mstart, finish)
            mstart = finish.minusDays(2)
        }
        return ret
    }

    private fun loadSome(instrId: InstrId, start: LocalDateTime, finish : LocalDateTime): List<Ohlc> {
        while (System.currentTimeMillis() - lastFinamCall < 1100) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }

        }

        lastFinamCall = System.currentTimeMillis()

        val params = listOf(
                "d" to "d",
                "f" to "table",
                "e" to ".csv",
                "dtf" to "1",
                "tmf" to "3",
                "MSOR" to "0",
                "mstime" to "on",
                "mstimever" to "1",
                "sep" to "3",
                "sep2" to "1",
                "at" to "1",
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
                "cn" to "${instrId.code}"
        )

        val url = "http://export.finam.ru/table.csv?" + params.map { "${it.first}=${it.second}" }.joinToString(separator = "&")

        //http://export.finam.ru/table.csv?d=d&f=table&e=.csv&dtf=1&tmf=3&MSOR=0&mstime=on&mstimever=1&sep=3&sep2=1&at=1&p=4&em=81820&market=1&df=1&mf=0&yf=2017&dt=15&mt=0&yt=2019&cn=ALRS&code=ALRS&datf=5
        //http://export.finam.ru/table.csv?d=d&f=table&e=.csv&dtf=1&tmf=3&MSOR=0&mstime=on&mstimever=1&sep=3&sep2=1&at=1&p=4&em=81112&market=32&df=25&mf=4&yf=2017&dt=15&mt=0&yt=2019&code=alrs&cn=alrsMdDao

        val ret = SettableFuture.create<List<String>>()
        print(url)
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
            val ret = ret.get().map { Ohlc.parse(it) }.filter { it.isPresent }.map { it.get() }
            return ret
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun getName(): String {
        return SOURCE
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min10
    }

    companion object {
        private val log = LoggerFactory.getLogger(FinamDownloader::class.java)
        val SOURCE = "FINAM"

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
    }
}

