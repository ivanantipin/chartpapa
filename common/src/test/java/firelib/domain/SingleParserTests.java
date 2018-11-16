package firelib.domain;

import firelib.common.misc.dateUtils;
import firelib.parser.CsvParser;
import firelib.parser.LegacyMarketDataFormat;
import firelib.parser.LegacyMarketDataFormatLoader;
import firelib.parser.ParserHandlersProducer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


public class SingleParserTests {

    /**
     * using testresources folder to prevent files copying every build
     */
    String getfile(String name) {
        return Paths.get("./src/test/testresources", name).toAbsolutePath().toString();
    }

    private ParserHandlersProducer getStdNyTickSettings() {
        LegacyMarketDataFormat legacyMarketDataFormat = new LegacyMarketDataFormat();
        legacyMarketDataFormat.DATEFORMAT = "DD.MM.YYYY";
        legacyMarketDataFormat.TIMEFORMAT = "HHMMSS";
        legacyMarketDataFormat.COLUMNFORMAT = new String[]{"D", "T", "#", "P", "V", "U", "B", "A", "I"};
        legacyMarketDataFormat.TIMEZONE = "NY";
        return new ParserHandlersProducer(LegacyMarketDataFormatLoader. transform(legacyMarketDataFormat));
    }

    private ParserHandlersProducer getStdFortsTickSettings() {
        //
        // Check that FORTS FUT format parsing works.
        //
        LegacyMarketDataFormat legacyMarketDataFormat = new LegacyMarketDataFormat();
        legacyMarketDataFormat.DATEFORMAT = "YYYY-MM-DD";
        legacyMarketDataFormat.TIMEFORMAT = "HH:MM:SS";
        legacyMarketDataFormat.COLUMNFORMAT = new String[] {"D", "T", "#", "P", "V", "I"};
        legacyMarketDataFormat.TIMEZONE = "MOSCOW";
        return new ParserHandlersProducer(LegacyMarketDataFormatLoader.transform(legacyMarketDataFormat));
    }

    @Test
    public void TestUltraFastSingleCsvParserStartEndTimes() {


        ParserHandlersProducer parserHandlersProducer = getStdNyTickSettings();

        CsvParser<Tick> csvParser = new CsvParser<>(getfile("UltraFastParser/TstData2/data1_0.csv"), parserHandlersProducer.handlers, () -> new Tick());

        Instant dt0 = LocalDateTime.of(2011, 11, 21, 2, 0, 8, 700_000_000).atZone(parserHandlersProducer.zoneId).toInstant();
        Instant dt1 = LocalDateTime.of(2012, 9, 26, 3, 55, 21, 222_000_000).atZone(parserHandlersProducer.zoneId).toInstant();

        Assert.assertEquals(csvParser.startTime(), dt0);
        Assert.assertEquals(csvParser.endTime(), dt1);
    }


    @Test
    public void TestUltraFastSingleCsvParserStartEndTimesSingleLine() {
        ParserHandlersProducer parserHandlersProducer = getStdNyTickSettings();

        CsvParser<Tick> csvParser = new CsvParser<>(getfile("UltraFastParser/TstData2/data2_0.csv"), parserHandlersProducer.handlers, () -> new Tick());

        Instant dt0 = LocalDateTime.of(2011, 11, 21, 2, 0, 8, 700_000_000).atZone(parserHandlersProducer.zoneId).toInstant();

        Assert.assertEquals(csvParser.startTime(), dt0);
        Assert.assertEquals(csvParser.endTime(), dt0);
    }

    @Test
    public void TestUltraFastSingleCsvParser_3() {
        //
        // Test that two parsers are able to open the same .csv file simultaneously.
        //

        ParserHandlersProducer parserHandlersProducer = getStdNyTickSettings();

        CsvParser<Tick> csvParser = new CsvParser<>(getfile("UltraFastParser/TstData2/data2_0.csv"), parserHandlersProducer.handlers, () -> new Tick());

        CsvParser<Tick> csvParser1 = new CsvParser<>(getfile("UltraFastParser/TstData2/data2_0.csv"), parserHandlersProducer.handlers, () -> new Tick());

        Instant dt0 = LocalDateTime.of(2011, 11, 21, 2, 0, 8, 700_000_000).atZone(parserHandlersProducer.zoneId).toInstant();

        Assert.assertEquals(csvParser.startTime(), dt0);
        Assert.assertEquals(csvParser.endTime(), dt0);

        Assert.assertEquals(csvParser1.startTime(), dt0);
        Assert.assertEquals(csvParser1.endTime(), dt0);


    }

    @Test
    public void TestUltraFastSingleCsvParser_IQFEED_6() {
        //
        // Check that IQFEED format parsing works.
        //

        ParserHandlersProducer parserHandlersProducer = getStdNyTickSettings();

        CsvParser<Tick> csvParser = new CsvParser<>(getfile("UltraFastParser/TstData2/data2_0.csv"), parserHandlersProducer.handlers, () -> new Tick());



        Assert.assertTrue(csvParser.seek(csvParser.startTime()));

        Instant dt0 = LocalDateTime.of(2011, 11, 21, 2, 0, 8, 700_000_000).atZone(parserHandlersProducer.zoneId).toInstant();

        Assert.assertEquals(csvParser.current().time(), dt0);
    }


    Instant getTime(int y, int month, int d, int h, int m, int s, int mil, ZoneId zoneId){
        return LocalDateTime.of(y, month, d, h, m, s, mil*1000_000).atZone(zoneId).toInstant();
    }

    @Test
    public void TestUltraFastSingleCsvParser_FORTS_FUT()
    {

        ParserHandlersProducer parserHandlersProducer = getStdFortsTickSettings();


        CsvParser<Tick> csvParser = new CsvParser<>(getfile("UltraFastParser/Seek2/RI#_0.csv"), parserHandlersProducer.handlers, () -> new Tick());

        Assert.assertTrue(csvParser.seek(csvParser.startTime()));


        Assert.assertEquals(csvParser.current().time(), getTime(2012, 1, 3, 10, 0, 0, 53, parserHandlersProducer.zoneId));
        Assert.assertEquals(csvParser.current().last(), 137495,0.00001);
        Assert.assertEquals(csvParser.current().vol(), 1);
//        Assert.assertEquals(parser.CurrentQuote().Ask(), 0,0.00001);
  //      Assert.assertEquals(parser.CurrentQuote().Bid(), 0,0.00001);
        //FIXME Assert.assertEquals(parser.CurrentQuote().CumVol, 0);
        Assert.assertEquals(csvParser.current().getTickNumber(), 483738513l);

        Assert.assertTrue(csvParser.read());


        Assert.assertEquals(csvParser.current().time(), getTime(2012, 1, 3, 10, 0, 0, 53, parserHandlersProducer.zoneId));
        Assert.assertEquals(csvParser.current().last(), 137500,0.00001);
        Assert.assertEquals(csvParser.current().vol(), 1);
    //    Assert.assertEquals(parser.CurrentQuote().Ask(), 0,0.00001);
     //   Assert.assertEquals(parser.CurrentQuote().Bid(), 0,0.00001);
        //FIXME Assert.assertEquals(parser.CurrentQuote().CumVolume, 0);
        Assert.assertEquals(csvParser.current().tickNumber(), 483738514l);
    }

    @Test
    public void testLongFileTicks() {
        ParserHandlersProducer parserHandlersProducer = new ParserHandlersProducer(LegacyMarketDataFormatLoader.transform(new LegacyMarketDataFormat().loadFromFile(getfile("LongFile/common.ini"))));
        CsvParser<Tick> csvParser = new CsvParser<>(getfile("LongFile/XG_#.csv"), parserHandlersProducer.handlers, () -> new Tick());
        int cnt = 0;
        while (csvParser.read()){
            cnt+=1;
        }
        Assert.assertEquals(345600,cnt);
    }

    @Test
    public void testSearch() {
        LegacyMarketDataFormat settings = new LegacyMarketDataFormat().loadFromFile(getfile("LongFile/common.ini"));
        ParserHandlersProducer parserHandlersProducer = new ParserHandlersProducer(LegacyMarketDataFormatLoader.transform(settings));
        CsvParser<Tick> csvParser = new CsvParser<>(getfile("LongFile/XG_#.csv"), parserHandlersProducer.handlers, () -> new Tick());

        Instant instant = dateUtils.parseAtZone("11.03.2013 23:59:59",dateUtils.nyZoneId());
        csvParser.seek(instant);
        Assert.assertEquals(instant, csvParser.current().dtGmt());

    }


    @Test
    public void testDukasFileTicks() {
        LegacyMarketDataFormat settings = new LegacyMarketDataFormat().loadFromFile(getfile("dukas/common.ini"));
        ParserHandlersProducer parserHandlersProducer = new ParserHandlersProducer(LegacyMarketDataFormatLoader.transform(settings));

        CsvParser<Tick> csvParser = new CsvParser<>(getfile("dukas/audnzd.csv"), parserHandlersProducer.handlers, () -> new Tick());
        int cnt = 0;
        while (csvParser.read()){
            cnt+=1;
        }
        Assert.assertEquals(1000,cnt);
    }




}
