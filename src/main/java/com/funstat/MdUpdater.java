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
import java.util.stream.Collectors;

public class MdUpdater {

    Map<String, Source> sources = new HashMap<String, Source>() {
        {
            put(FinamDownloader.FINAM, new FinamDownloader());
            put(VantageDownloader.SOURCE, new VantageDownloader());
        }
    };

    MdDao mdDao = new MdDao();
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);


    public void start() {
        executor.scheduleAtFixedRate(this::run, 0, 10, TimeUnit.MINUTES);
    }

    List<Symbol> metaCache = null;

    public List<Symbol> getMeta() {
        if (metaCache == null) {
            this.metaCache = sources.values().stream().flatMap(s -> s.symbols().stream()).collect(Collectors.toList());
        }
        return metaCache;
    }


    List<Symbol> getCodesToUpdate() {
        return mdDao.read("requested", String.class).stream().map(code->findSymbol(code)).collect(Collectors.toList());
    }

    Symbol findSymbol(String code) {
        return getMeta().stream().filter(s -> {
            return s.code.equals(code) && (s.market.equals("1")
                    || s.market.equals("MICEX"));
        }).findFirst().get();
    }

    public void run() {
        getCodesToUpdate().forEach(symbol -> {
            update(symbol);
        });
    }

    List<Ohlc> get(String code) {

        Symbol symbol = findSymbol(code);

        mdDao.saveGeneric("requested", Collections.singletonList(symbol), s -> s.code);

        List<Ohlc> ret = mdDao.queryAll(symbol.tableToPersist());
        if (ret.isEmpty()) {
            update(symbol);
        }
        return ret;
    }

    public void update(Symbol symbol) {

        LocalDateTime startTime = mdDao.queryLast(symbol.tableToPersist()).map(oh -> oh.dateTime.minusDays(2)).orElse(LocalDateTime.now().minusDays(600));

        List<Ohlc> data = sources.get(symbol.source).load(symbol, startTime);

        Map[] maps = data.stream().map(oh -> {
            return new HashMap<String, Object>() {
                {
                    put("DT", Timestamp.valueOf(oh.dateTime));
                    put("OPEN", oh.open);
                    put("HIGH", oh.high);
                    put("LOW", oh.low);
                    put("CLOSE", oh.close);
                }
            };
        }).toArray(sz -> new Map[sz]);
        System.out.println("to be inserted/updated " + maps.length + " into " + symbol.tableToPersist());
        try {
            mdDao.save(symbol.tableToPersist(), maps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("DONE " + symbol.code);
    }
}
