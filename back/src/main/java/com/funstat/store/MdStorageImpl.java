package com.funstat.store;


import com.funstat.Pair;
import com.funstat.Tables;
import com.funstat.domain.Ohlc;
import com.funstat.finam.FinamDownloader;
import com.funstat.domain.InstrId;
import com.funstat.iqfeed.IntervalTransformer;
import com.funstat.iqfeed.IqFeedSource;
import com.funstat.vantage.Source;
import com.funstat.vantage.VSymbolDownloader;
import com.funstat.vantage.VantageDownloader;
import firelib.common.interval.Interval;
import org.apache.commons.io.FileUtils;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MdStorageImpl implements MdStorage {

    public static final String HOME_PATH = "/ddisk/globaldatabase/md";
    private String folder;

    SingletonsContainer container = new SingletonsContainer();

    Map<String, Source> sources = new HashMap<String, Source>() {
        {
            put(FinamDownloader.SOURCE, new FinamDownloader());
            //put(VantageDownloader.SOURCE, new VantageDownloader());
            put(IqFeedSource.Companion.getSOURCE(), new IqFeedSource(Paths.get("/ddisk/globaldatabase/1MIN/STK")));
        }
    };


    public static final String SYMBOLS_TABLE = "symbols";
    public static final String SYMBOLS_LAST_UPDATED = "SYMBOLS_LAST_UPDATED";
    private static final String VANTAGE_LAST_UPDATED = "VANTAGE_LAST_UPDATED";

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public MdStorageImpl(String folder) {
        this.folder = folder;
        try {
            FileUtils.forceMkdir(new File(folder));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    GenericDao getGeneric(){
        return container.get("generic dao", ()->{
            return new GenericDaoImpl(getDsForFile(folder + "/meta.db"));
        });
    }

    MdDao getDao(String source, String interval){
        return container.get(source + "/" + interval , ()->{
            String folder = this.folder + "/" + source + "/";
            try {
                FileUtils.forceMkdir(new File(folder));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            SQLiteDataSource ds = getDsForFile(folder + interval + ".db");
            return new MdDao(ds);
        });
    }

    public static SQLiteDataSource getDsForFile(String file) {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + file);
        return ds;
    }


    @Override
    public List<Ohlc> read(InstrId instrId, String interval){
        Tables.REQUESTED.writeSingle(getGeneric(), instrId);
        MdDao dao = getDao(instrId.source, sources.get(instrId.source).getDefaultInterval().name());
        List<Ohlc> ret = dao.queryAll(instrId.code);
        Interval target = Interval.valueOf(interval);
        if (ret.isEmpty()) {
            updateMarketData(instrId);
            ret = dao.queryAll(instrId.code);
        }
        long start = System.currentTimeMillis();
        try{
            return IntervalTransformer.transform(target,ret);
        }finally {
            System.out.println("transformed in " + (System.currentTimeMillis() - start)/1000.0 + " s. " + ret.size() + " min bars");
        }
    }

    @Override
    public void save(String code, String source, String interval, List<firelib.domain.Ohlc> data){
        getDao(source, interval).insert(data, code);
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            try {
                Tables.REQUESTED.read(getGeneric()).forEach(symbol -> {
                    updateMarketData(symbol);
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    public void updateSymbolsMeta(){
        Pair lastUpdated = Tables.PAIRS.readByKey(getGeneric(), SYMBOLS_LAST_UPDATED);
        if(lastUpdated == null ||  (System.currentTimeMillis() - Long.parseLong(lastUpdated.value)) > 24*3600_000){
            System.out.println("updating symbols as they are stale");
            getGeneric().saveGeneric(SYMBOLS_TABLE, sources.values().stream()
                    .flatMap(s -> s.symbols().stream()).filter(s -> {
                        return s.market.equals("1") || s.source.equals(VantageDownloader.SOURCE) || s.source.equals(IqFeedSource.Companion.getSOURCE());
                    }).collect(Collectors.toList()), s->s.code);
            Tables.PAIRS.writeSingle(getGeneric(), new Pair(SYMBOLS_LAST_UPDATED,"" + System.currentTimeMillis()));
        }else {
            System.out.println("not updating symbols as last update was " + LocalDateTime.ofEpochSecond(Long.parseLong(lastUpdated.value)/1000, 0, ZoneOffset.UTC));
        }

        if(false){
            //fixme
            updateVantage();
        }

    }

    private void updateVantage() {
        Pair lastVantageUpdated = Tables.PAIRS.readByKey(getGeneric(), VANTAGE_LAST_UPDATED);
        if(lastVantageUpdated == null){
            ExecutorService exec = Executors.newSingleThreadExecutor();
            exec.submit(()->{
                VSymbolDownloader.updateVantageSymbols(getGeneric());
                Tables.PAIRS.writeSingle(getGeneric(), new Pair(VANTAGE_LAST_UPDATED, "" + System.currentTimeMillis()));
            });
        }
    }


    public List<InstrId> getMeta() {
        return container.get(SYMBOLS_TABLE, () -> Tables.SYMBOLS.read(getGeneric()));
    }


    public void updateMarketData(InstrId instrId) {
        Source source = sources.get(instrId.source);
        MdDao dao = getDao(instrId.source, source.getDefaultInterval().name());
        LocalDateTime startTime = dao.queryLast(instrId.code).map(oh -> oh.dateTime.minusDays(2)).orElse(LocalDateTime.now().minusDays(600));
        dao.insertJ(source.load(instrId, startTime), instrId.code);
    }

    public static void main(String[] args) {
        MdStorageImpl mdStorage = new MdStorageImpl(HOME_PATH);
        mdStorage.updateSymbolsMeta();
        System.out.println(mdStorage.getMeta().stream().filter(s->s.source.equals(FinamDownloader.SOURCE)));

    }

}
