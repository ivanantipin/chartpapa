package com.funstat.iqfeed

import firelib.domain.Ohlc
import com.funstat.domain.InstrId
import com.funstat.vantage.Source
import firelib.common.interval.Interval
import firelib.parser.CsvParser
import firelib.parser.LegacyMarketDataFormatLoader
import firelib.parser.ParseHandler
import firelib.parser.ParserHandlersProducer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicLong

class IqFeedSource(val csvPath: Path) : Source {

    //val dir = Paths.get("/ddisk/globaldatabase/1MIN/STK")

    override fun symbols(): List<InstrId> {
        val map = code2name()
        return csvPath.toFile().list().map {
            it.replace("_1.csv", "")
        }.filter { map.containsKey(it) }.map { InstrId(it, map[it], "NA", it, SOURCE) }
    }

    override fun load(instrId: InstrId): List<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(600));
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): List<Ohlc> {
        val cnt = AtomicLong()
        val iniFile = csvPath.resolve("common.ini").toAbsolutePath().toString()
        val load = LegacyMarketDataFormatLoader.load(iniFile)
        val producer = ParserHandlersProducer(load)
        val fname = csvPath.resolve(instrId.code + "_1.csv").toAbsolutePath().toString()
        val ohlcs = ArrayList<firelib.domain.Ohlc>()
        try {
            val parser = CsvParser<firelib.domain.Ohlc>(fname, producer.handlers as Array<out ParseHandler<firelib.domain.Ohlc>>?, { firelib.domain.Ohlc() }, 10_000_000)
            println(parser.seek(dateTime.toInstant(ZoneOffset.UTC)))
            while (parser.read()) {
                ohlcs.add(parser.current())
                cnt.incrementAndGet()
            }
        } catch (e: Exception) {
        }

        return ohlcs.map { ohlc -> Ohlc(ohlc.dtGmtEnd, ohlc.open, ohlc.high, ohlc.low, ohlc.close) };

    }

    override fun getName(): String {
        return SOURCE
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min1
    }

    companion object {
        val SOURCE = "IQFEED"
        fun code2name(): Map<String, String> {
            try {
                val lines = Files.readAllLines(Paths.get(IqFeedSource::class.java.getResource("/iqfeed_symbols.txt").toURI()))
                return lines.map { it.split(";") }.associateBy({ it[0] }, { it[1] })
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            println(IqFeedSource(Paths.get("/ddisk/globaldatabase/1MIN/STK")).symbols())
        }
    }
}
