package com.funstat.iqfeed;

import com.funstat.domain.Ohlc;
import com.funstat.finam.Symbol;
import com.funstat.store.MdDao;
import com.funstat.vantage.Source;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class IqFeedSource implements Source {

    public static final String SOURCE = "SOURCE";

    MdDao dao = null;



    @Override
    public List<Symbol> symbols() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(IqFeedSource.class.getResource("/iqfeed_symbols.txt").toURI()));
            return lines.stream().skip(1).map(l->{
                String[] arr = l.split(";");
                return new Symbol(arr[0],arr[1],"NA",arr[0], SOURCE);
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Ohlc> load(Symbol symbol) {
        return dao.queryAll(symbol.code);
    }

    @Override
    public List<Ohlc> load(Symbol symbol, LocalDateTime dateTime) {
        return dao.queryAll(symbol.code);
    }

    @Override
    public String getName() {
        return SOURCE;
    }

    public static void main(String[] args) {
        new IqFeedSource().symbols().stream().forEach(s->{
            System.out.println("+ 1MIN/STK/" + s.code + "_*");
        });

    }
}
