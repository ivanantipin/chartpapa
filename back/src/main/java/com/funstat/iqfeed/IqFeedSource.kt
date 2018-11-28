package com.funstat.iqfeed

import com.funstat.domain.Ohlc
import com.funstat.domain.InstrId
import com.funstat.store.MdDao
import com.funstat.vantage.Source
import firelib.common.interval.Interval
import firelib.common.misc.toLondonTime
import firelib.parser.CsvParser
import firelib.parser.LegacyMarketDataFormatLoader
import firelib.parser.ParseHandler
import firelib.parser.ParserHandlersProducer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors

class IqFeedSource(val csvPath : Path) : Source {

    //val dir = Paths.get("/ddisk/globaldatabase/1MIN/STK")

    override fun symbols(): List<InstrId> {
        try {
            val lines = Files.readAllLines(Paths.get(IqFeedSource::class.java.getResource("/iqfeed_symbols.txt").toURI()))
            return lines.stream().skip(1).map { l ->
                val arr = l.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                InstrId(arr[0], arr[1], "NA", arr[0], SOURCE)
            }.collect(Collectors.toList())

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun load(instrId: InstrId): List<Ohlc> {


        val cnt = AtomicLong()

        val iniFile = csvPath.resolve("common.ini").toAbsolutePath().toString()


        val load = LegacyMarketDataFormatLoader.load(iniFile)
        val producer = ParserHandlersProducer(load)


        val fname = csvPath.resolve(instrId.code + "_1.csv").toAbsolutePath().toString()
        val ohlcs = ArrayList<firelib.domain.Ohlc>()
        try {
            val parser = CsvParser<firelib.domain.Ohlc>(fname, producer.handlers as Array<out ParseHandler<firelib.domain.Ohlc>>?, { firelib.domain.Ohlc() }, 100000000)
            println(parser.seek(LocalDateTime.now().minusDays(600).toInstant(ZoneOffset.UTC)))


            while (parser.read()) {

                ohlcs.add(parser.current())
                cnt.incrementAndGet()

            }
        }catch (e : Exception){
        }

        return  ohlcs.map { ohlc -> Ohlc(LocalDateTime.ofInstant(ohlc.dtGmtEnd, ZoneOffset.UTC), ohlc.O,ohlc.H,ohlc.L,ohlc.C) };
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): List<Ohlc> {
        return load(instrId)
    }

    override fun getName(): String {
        return SOURCE
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min1
    }

    companion object {

        val SOURCE = "IQFEED"

        @JvmStatic
        fun main(args: Array<String>) {
            IqFeedSource(Paths.get("/ddisk/globaldatabase/1MIN/STK")).symbols().stream().forEach { s -> println("+ 1MIN/STK/" + s.code + "_*") }
        }
    }
}
