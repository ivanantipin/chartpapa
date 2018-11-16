package com.funstat.tssyncer;

import com.google.common.collect.Lists;
import firelib.domain.Ohlc;
import firelib.parser.CsvParser;
import firelib.parser.LegacyMarketDataFormatLoader;
import firelib.parser.MarketDataFormatMetadata;
import firelib.parser.ParserHandlersProducer;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TSSyncer {

    private final DataSource ds;
    private final DataSourceTransactionManager manager;

    public TSSyncer() {
        ds = getLiteDs();
        manager = new DataSourceTransactionManager(ds);
    }

    DataSource getDs(){
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName("localhost");
        source.setDatabaseName("mddb");
        source.setUser("ivan");
        source.setPassword("MAI28mama10");
        source.setSendBufferSize(10_000_000);
        source.setBinaryTransfer(true);
//        source.setAutosave(AutoSave.ALWAYS);
        source.setDefaultRowFetchSize(50000);
        source.setPreparedStatementCacheQueries(10);
        source.setMaxConnections(10);
        return source;
    }

    DataSource getLiteDs(){
        SQLiteDataSource ds = new SQLiteDataSource();

        {
            ds.setUrl("jdbc:sqlite:/ddisk/globaldatabase/liteTemp/toBeRemoved.db");
            //new JdbcTemplate(ds).execute("create table if not exists ohlc (source varchar, code varchar, dt datetime, o number,h number,l number ,c number, primary key (source,code,dt)) ");
        }
        return ds;
    }

    void createTable(String table){
        JdbcTemplate template = new JdbcTemplate(ds);
        template
                .execute("create table if not exists " + table +
                        " (dt TIMESTAMPTZ, " +
                        "o DOUBLE PRECISION  not NULL," +
                        "h DOUBLE PRECISION  not NULL," +
                        "l DOUBLE PRECISION  not NULL," +
                        "c DOUBLE PRECISION  not NULL," +
                        "primary key (dt)) ;");

        //template.execute("select create_hypertable('" + table + "' , 'dt');");
    }



    private void saveInTransaction(String sql, List<Map<String, Object>> data) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.execute(status -> {
            long start = System.currentTimeMillis();
            NamedParameterJdbcTemplate template1 = new NamedParameterJdbcTemplate(ds);
            Lists.partition(data,100000).forEach(ll->{
                template1.batchUpdate(sql, ll.toArray(new Map[ll.size()]));
            });
            double dur = (System.currentTimeMillis() - start) / 1000.0;
            System.out.println("inserting " + data.size() + " took " + dur + " sec ," + " rate is " +
            data.size()/dur + " per sec");
            return null;
        });
    }

    public List<Ohlc> readTable(String table){
        return new JdbcTemplate(ds).query("select * from " + table + " order by dt", (r,num)->{
            Timestamp ts = r.getTimestamp("dt");
            Ohlc ret = new Ohlc();
            ret.setDtGmtEnd(ts.toInstant());
            ret.setO(r.getDouble("o"));
            ret.setH(r.getDouble("h"));
            ret.setL(r.getDouble("l"));
            ret.setC(r.getDouble("c"));
            return ret;
        });
    }


    public void insert(List<Ohlc> ohlcs, String table){
        List<Map<String, Object>> data = ohlcs.stream().map(oh -> {
            return new HashMap<String, Object>() {
                {
                    put("DT", Timestamp.valueOf(LocalDateTime.ofInstant(oh.getDtGmtEnd(), ZoneOffset.UTC)));
                    put("OPEN", oh.getO());
                    put("HIGH", oh.getH());
                    put("LOW", oh.getL());
                    put("CLOSE", oh.getC());
                }
            };
        }).collect(Collectors.toList());

        try {
            saveInTransaction("insert into " + table + "(DT,O,H,L,C) values (:DT,:OPEN,:HIGH,:LOW,:CLOSE)", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {


        TSSyncer syncer = new TSSyncer();


/*
        long start = System.currentTimeMillis();
        List<Ohlc> list = syncer.readTable("aapl");

        System.out.println("size is " + list.size() + " in " + (System.currentTimeMillis() - start));
*/


        ssync(syncer);

/*




        parser.seek(Instant.MIN);




        while (parser.read()){

            Ohlc oh = parser.current();



        }
*/




    }

    private static void ssync(TSSyncer syncer) {
        Path dir = Paths.get("/ddisk/globaldatabase/1MIN/STK");

        AtomicLong cnt = new AtomicLong();

        String iniFile = dir.resolve("common.ini").toAbsolutePath().toString();


        MarketDataFormatMetadata load = LegacyMarketDataFormatLoader.load(iniFile);
        ParserHandlersProducer producer = new ParserHandlersProducer(load);


        //val ret: CsvParser[T] = new CsvParser[T](path.toAbsolutePath.toString, generator.handlers.asInstanceOf[Array[ParseHandler[T]]], factory)


        for(String s : dir.toFile().list((f,b)->{
            return b.endsWith("csv");
        })){
            try {
                String table = s.replaceAll(".csv", "").replaceAll("_1", "");
                syncer.createTable(table);
                String fname = dir.resolve(s).toAbsolutePath().toString();
                List<Ohlc> ohlcs = new ArrayList<>();
                try {
                    CsvParser<Ohlc> parser = new CsvParser<>(fname, producer.handlers, ()->new Ohlc(), 100000000);
                    System.out.println(parser.seek(Instant.MIN));


                    while (parser.read()){

                        ohlcs.add(parser.current());
                        cnt.incrementAndGet();

                    }

                    syncer.insert(ohlcs, table);

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
