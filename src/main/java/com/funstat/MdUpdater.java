package com.funstat;

import com.funstat.domain.Ohlc;
import com.funstat.finam.Symbol;
import com.funstat.finam.FinamDownloader;
import com.funstat.store.MdDao;
import com.funstat.vantage.Source;
import com.funstat.vantage.VantageDownloader;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MdUpdater {

    public static final String SYMBOLS_TABLE = "symbols";
    Map<String, Source> sources = new HashMap<String, Source>() {
        {
            put(FinamDownloader.FINAM, new FinamDownloader());
            put(VantageDownloader.SOURCE, new VantageDownloader());
        }
    };

    MdDao mdDao = new MdDao();
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    <T> T getThings(String key, Supplier<T> factory) {
        return (T) cache.computeIfAbsent(key, k -> {
            return factory.get();
        });
    }


    public void start() {
        executor.scheduleAtFixedRate(this::run, 0, 10, TimeUnit.MINUTES);
    }

    public void updateSymbols(){
        mdDao.saveGeneric(SYMBOLS_TABLE, sources.values().stream()
                .flatMap(s -> s.symbols().stream()).filter(s -> {
                    return s.market.equals("1") || s.source.equals(VantageDownloader.SOURCE);
                }).collect(Collectors.toList()), s->s.code);
    }


    public List<Symbol> getMeta() {
        return getThings(SYMBOLS_TABLE, () -> mdDao.readGeneric(SYMBOLS_TABLE, Symbol.class));
    }


    List<Symbol> getCodesToUpdate() {
        return mdDao.readGeneric("requested", Symbol.class);
    }

    Symbol findSymbol(String code) {
        return getMeta().stream().filter(f -> f.code.equals(code)).findFirst().get();
    }

    public void run() {
        System.out.println("running aoeuaoeu");
        try {
            getCodesToUpdate().forEach(symbol -> {
                update(symbol);
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    List<Ohlc> get(String code) {

        Symbol symbol = findSymbol(code);

        mdDao.saveGeneric("requested", Collections.singletonList(symbol), s -> s.code);

        List<Ohlc> ret = mdDao.queryAll(symbol.code, symbol.source);
        if (ret.isEmpty()) {
            update(symbol);
            ret = mdDao.queryAll(symbol.code, symbol.source);
        }
        return ret;
    }


    ExecutorService updateExecutor = Executors.newSingleThreadExecutor();

    void scheduleUpdate(Symbol symbol){


    }


    public void update(Symbol symbol) {
        LocalDateTime startTime = mdDao.queryLast(symbol.code, symbol.source).map(oh -> oh.dateTime.minusDays(2)).orElse(LocalDateTime.now().minusDays(600));
        mdDao.saveOhlc(symbol,sources.get(symbol.source).load(symbol, startTime));
    }

    public static void main(String[] args) {
        new MdUpdater().updateSymbols();
    }
}
