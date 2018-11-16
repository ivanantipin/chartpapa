package firelib.common

import java.nio.file.{Files, Path, Paths}
import java.util.function.Supplier

import firelib.common.reader.binary.TickDesc
import firelib.common.reader.{CachedService, MarketDataReader}
import firelib.domain.Tick
import firelib.parser.{CsvParser, LegacyMarketDataFormatLoader, ParseHandler, ParserHandlersProducer}
import org.apache.commons.io.FileUtils
import org.junit.{Assert, Test}

class CachedServiceTest {
    /**
     * using testresources folder to prevent files copying every build
     */
    private def getfile(name: String): String = {
        return Paths.get("./src/test/testresources", name).toAbsolutePath.toString
    }

    @Test def testTicksCicle {

        val cacheRoot: Path = Paths.get("./src/test/testresources/cached")

        System.err.print(cacheRoot.toAbsolutePath.toString)

        val service = new CachedService(cacheRoot.toString)

        FileUtils.deleteDirectory(cacheRoot.toFile)

        Files.createDirectory(cacheRoot)

        val relDir = "testCached/ticks"

        val fname: String = getfile(s"${relDir}/ticks.csv")


        val parserHandlersProducer: ParserHandlersProducer = new ParserHandlersProducer(LegacyMarketDataFormatLoader.load(getfile(s"${relDir}/common.ini")))

        val parser: CsvParser[Tick] = new CsvParser[Tick](fname, parserHandlersProducer.handlers.asInstanceOf[Array[ParseHandler[Tick]]], new Supplier[Tick] {
            override def get(): Tick = new Tick()
        })

        Assert.assertFalse(service.checkPresent(fname,parser.startTime(),parser.endTime(), new TickDesc).isDefined)
        service.write(fname,parser, new TickDesc)
        val present: Option[MarketDataReader[Tick]] = service.checkPresent(fname, parser.startTime(), parser.endTime(), new TickDesc)

        Assert.assertTrue(present.isDefined)

        val binReader: MarketDataReader[Tick] = present.get

        val parser1 : CsvParser[Tick] = new CsvParser[Tick](fname, parserHandlersProducer.handlers.asInstanceOf[Array[ParseHandler[Tick]]], new Supplier[Tick] {
            override def get(): Tick = new Tick()
        })

        Assert.assertEquals(parser1.startTime(),binReader.startTime())
        Assert.assertEquals(parser1.endTime(),binReader.endTime())


        Assert.assertTrue(binReader.seek(parser1.endTime()))
        Assert.assertTrue(parser1.seek(parser1.endTime()))


        cmpCurrent(binReader, parser1)

        Assert.assertTrue(binReader.seek(parser1.startTime()))
        Assert.assertTrue(parser1.seek(parser1.startTime()))


        while (parser1.read()){
            Assert.assertTrue(binReader.read())
            cmpCurrent(binReader, parser1)
        }
    }

    def cmpCurrent(binReader: MarketDataReader[Tick], parser1: CsvParser[Tick]) {
        Assert.assertEquals(parser1.current().dtGmt, binReader.current.dtGmt)
        Assert.assertEquals(parser1.current().bid, binReader.current.bid, 0.0000001)
        Assert.assertEquals(parser1.current().ask, binReader.current.ask, 0.0000001)
    }
}
