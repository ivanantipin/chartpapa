package com.funstat.iqfeed;

import com.funstat.store.MdStorageImpl;
import firelib.common.interval.Interval;
import firelib.domain.Ohlc;
import firelib.parser.CsvParser;
import firelib.parser.LegacyMarketDataFormatLoader;
import firelib.parser.MarketDataFormatMetadata;
import firelib.parser.ParserHandlersProducer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DbPopulator {

    public static void main(String[] args) throws Exception {
        MdStorageImpl syncer = new MdStorageImpl("/ddisk/globaldatabase/md");
        ssync(syncer);
    }

    private static void ssync(MdStorageImpl syncer) {
        Path dir = Paths.get("/ddisk/globaldatabase/1MIN/STK");
        AtomicLong cnt = new AtomicLong();
        String iniFile = dir.resolve("common.ini").toAbsolutePath().toString();

        MarketDataFormatMetadata load = LegacyMarketDataFormatLoader.load(iniFile);
        ParserHandlersProducer producer = new ParserHandlersProducer(load);

        for(String s : dir.toFile().list((f,b)->{
            return b.endsWith("csv");
        })){
            try {
                String table = s.replaceAll(".csv", "").replaceAll("_1", "");

                String fname = dir.resolve(s).toAbsolutePath().toString();
                List<Ohlc> ohlcs = new ArrayList<>();
                try {
                    CsvParser<Ohlc> parser = new CsvParser<>(fname, producer.handlers, ()->new Ohlc(), 100000000);
                    System.out.println(parser.seek(Instant.MIN));


                    while (parser.read()){
                        ohlcs.add(parser.current());
                        cnt.incrementAndGet();
                    }
                    syncer.save(table, "IQFEED", Interval.Min1().name(), ohlcs);
                    System.out.println("done " + fname + " cnt is " + cnt);

                }catch (Exception e){
                    System.out.println("failed " + s + " due to " + e.getMessage());
                    e.printStackTrace();
                }
            }catch (Exception e){
                System.out.println("failed for " + s + " due to " + e.getMessage());
            }
        }
    }
}
