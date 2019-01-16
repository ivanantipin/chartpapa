package com.firelib.test

import firelib.common.MarketDataType
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.runSimple
import firelib.common.reader.ReaderFactoryImpl
import firelib.domain.Ohlc
import firelib.parser.CsvParser
import firelib.parser.LegacyMarketDataFormatLoader
import firelib.parser.ParseHandler
import firelib.parser.ParserHandlersProducer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId.of
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class BacktestIntegrationTest {

    //FIXME backtest on 2 instruments with several bars intervals

    val zoneId = of("America/New_York")

    fun getUsTime(y: Int, month: Int, d: Int, h: Int, m: Int, s: Int, mil: Int): Instant {
        val ret: Instant = LocalDateTime.of(y, month, d, h, m, s, mil * 1000000).atZone(zoneId).toInstant()
        return ret
    }


    fun getDsRoot(): String {
        return Paths.get("/home/ivan/projects/fbackend/back/src/test/testresources/TestRoot/testDsRoot").toAbsolutePath().toString()
    }

    fun getReportDir(): String {
        return Paths.get("/home/ivan/projects/fbackend/back/src/test/testresources/TestRoot/testReportDir").toAbsolutePath().toString()
    }


    @Test
    fun IntegrationTestTestMins() {


        val fileName = "MINS/XG_#.csv"
        val fullFileName: Path = Paths.get(getDsRoot() + "/" + fileName)
        val iniPath: Path = fullFileName.getParent().resolve("common.ini")

        var d0Gmt = LocalDateTime.of(2013, 3, 8, 5, 0, 0, 0).toInstant(ZoneOffset.UTC)

        var d0 = getUsTime(2013, 3, 8, 0, 0, 0, 0)

        Assert.assertTrue(d0 == d0Gmt)
        var d1 = getUsTime(2013, 3, 9, 0, 0, 0, 0)

        var d2 = getUsTime(2013, 3, 11, 0, 0, 0, 0)
        var d3 = getUsTime(2013, 3, 12, 0, 0, 0, 0)




        val quotesNumbers: Pair<Int, Int> = createFiles(fullFileName, Pair(d0,d1), Pair(d2,d3), this::ohlcGen, 5 * 60 * 1000)

        var totalQuotesNumber = quotesNumbers.first + quotesNumbers.second;


        val generator: ParserHandlersProducer = ParserHandlersProducer(LegacyMarketDataFormatLoader.load(iniPath.toString()))

        val pp = CsvParser<Ohlc>(fullFileName.toString(), generator.handlers as Array<ParseHandler<Ohlc>>) { Ohlc() }

        pp.seek(d0)

        var directBars = mutableListOf<Ohlc>()

        do {
            directBars.add(pp.current())
        } while (pp.read())


        val cfg = ModelBacktestConfig()


        cfg.dataServerRoot = getDsRoot()
        cfg.reportTargetPath = getReportDir()


        val factoryImpl = ReaderFactoryImpl(cfg.dataServerRoot)

        cfg.instruments += InstrumentConfig("XG", {startTime->
            factoryImpl.create(fileName, startTime)
        })

        cfg.startDateGmt = LocalDateTime.of(2013, Month.MARCH, 8, 5, 0, 0).toInstant(ZoneOffset.UTC)


        cfg.precacheMarketData = false

        val launch = GlobalScope.launch { runSimple(cfg, { ctx, props -> OhlcTestModel(ctx) }) }

        while (launch.isActive){
            Thread.sleep(100)
        }


        var idx = -1
        val modelBars = testHelper.instanceOhlc!!.bars

        val size: Int = modelBars.filter({ !it.interpolated }).size

        println("diff " + directBars.map { it.dtGmtEnd }.toSet().minus(modelBars.map { it.dtGmtEnd }))
        //FIXME
        Assert.assertEquals("bars number", size, totalQuotesNumber - 1)

        var curTime = cfg.startDateGmt
        for (i in 0 until modelBars.size) {
            Assert.assertEquals(curTime, modelBars[i].dtGmtEnd)
            curTime = curTime.plusSeconds(5 * 60)
            if (!modelBars[i].interpolated) {
                idx += 1
            }
            if (idx != -1) {
                val rb = directBars[idx]
                Assert.assertEquals("wrong bar for index $idx", rb.open, modelBars[i].open, 0.00001)
                Assert.assertEquals(rb.high, modelBars[i].high, 0.00001)
                Assert.assertEquals(rb.low, modelBars[i].low, 0.00001)
                Assert.assertEquals(rb.close, modelBars[i].close, 0.00001)
            }
        }

        Assert.assertTrue("days number", testHelper.instanceOhlc!!.startTimesGmt.size == 5)
    }


    fun createFiles(fullFileName: Path, iterval0 : Pair<Instant,Instant>,  interval1 : Pair<Instant,Instant>, strGen: (LocalDateTime) -> String, interval: Long, writeToDisk: Boolean = false): Pair<Int, Int> {

        if (writeToDisk) {
            Files.deleteIfExists(fullFileName)

            Files.createDirectories(fullFileName.getParent())

            Files.createFile(fullFileName)
        }

        val lst = generateInterval(iterval0, interval, strGen)

        if (writeToDisk)
            Files.write(fullFileName, lst, StandardOpenOption.WRITE)

        var lst2 = generateInterval(interval1, interval, strGen)

        if (writeToDisk)
            Files.write(fullFileName, lst2, StandardOpenOption.APPEND)

        return Pair(lst.size, lst2.size)
    }

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy,HHmmss.SSS")

    fun generateInterval(period : Pair<Instant,Instant> , stepMillis: Long, strGen: (LocalDateTime) -> String): List<String> {
        var cnt = 0
        var cursor = period.first.atZone(zoneId).toLocalDateTime()
        val lst = ArrayList<String>()
        while (period.second.atZone(zoneId).toLocalDateTime().isAfter(cursor)) {
            lst += strGen(cursor)
            cursor = cursor.plusNanos(stepMillis * 1000000)
            cnt += 1
        }
        return lst
    }


    fun ohlcGen(cursor: LocalDateTime): String {
        var cl = cursor.toLocalTime().toSecondOfDay().toDouble() + 10
        var close = "%.2f".format(cl)
        var high = "%.2f".format(cl + 2)
        var low = "%.2f".format(cl - 2)
        var open = close
        val dt = formatter.format(cursor)
        return "$dt,$open,$high,$low,$close,1000,1"
    }


}
