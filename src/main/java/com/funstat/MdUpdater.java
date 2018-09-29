package com.funstat;

import com.funstat.domain.Ohlc;
import com.funstat.finam.Symbol;
import com.funstat.finam.FinamDownloader;
import com.funstat.store.MdDao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MdUpdater {

    FinamDownloader loader = new FinamDownloader();
    MdDao mdDao = new MdDao();


    List<Symbol> metaCache = null;

    List<Symbol> getMeta(){
        if(metaCache == null){
            this.metaCache = loader.readMeta();
            System.out.println("updated meta cache" + metaCache);
        }
        return metaCache;

    }

    List<String> getCodesToUpdate(){
        return Arrays.asList("TATN","ROSN","RASP","MGNT","NLMK");
    }

    List<Symbol> getUpdateSymbols(){
        return getMeta().stream().filter(s->{
            return getCodesToUpdate().contains(s.code) && s.market.equals("1");
        }).collect(Collectors.toList());
    }

    public void run(){
        getUpdateSymbols().forEach(symbol -> {
            List<Ohlc> data = loader.load(symbol, LocalDate.now().minusDays(600));

            Set<LocalDateTime> existed = mdDao.queryAll(symbol.code).stream().map(oh -> oh.dateTime).collect(Collectors.toSet());
            List<Ohlc> tbinserted = data.stream().filter(dt -> !existed.contains(dt.dateTime)).collect(Collectors.toList());

            Map[] maps = tbinserted.stream().map(oh -> {
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
            System.out.println("to be inserted " + maps.length + " into " + symbol.code);
            mdDao.save(symbol.code, maps);
        });
    }

    public static void main(String[] args) {
        new MdUpdater().run();
    }

}
