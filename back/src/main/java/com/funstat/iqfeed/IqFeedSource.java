package com.funstat.iqfeed;

import com.funstat.domain.Ohlc;
import com.funstat.domain.InstrId;
import com.funstat.store.MdDao;
import com.funstat.vantage.Source;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class IqFeedSource implements Source {

    public static final String SOURCE = "IQFEED";

    MdDao dao = null;

    @Override
    public List<InstrId> symbols() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(IqFeedSource.class.getResource("/iqfeed_symbols.txt").toURI()));
            return lines.stream().skip(1).map(l->{
                String[] arr = l.split(";");
                return new InstrId(arr[0],arr[1],"NA",arr[0], SOURCE);
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Ohlc> load(InstrId instrId) {
        return dao.queryAll(instrId.code);
    }

    @Override
    public List<Ohlc> load(InstrId instrId, LocalDateTime dateTime) {
        return dao.queryAll(instrId.code);
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
