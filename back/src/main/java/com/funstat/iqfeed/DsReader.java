package com.funstat.iqfeed;

import com.funstat.domain.Ohlc;
import firelib.common.interval.Interval;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DsReader {

    private DataSource dataSource;

    public DsReader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ohlc> readTable(String table){
        return new JdbcTemplate(dataSource).query("select * from " + table + " order by dt", (r, num)->{
            Timestamp ts = r.getTimestamp("dt");
            return new Ohlc(ts.toLocalDateTime(), r.getDouble("o"), r.getDouble("h"),r.getDouble("l"),r.getDouble("c"));
        });
    }

    public List<Ohlc> readIntervals(Interval interval, String symbol){
        long start = System.currentTimeMillis();
        List<Ohlc> ohlcs = readTable(symbol);
        System.out.println(System.currentTimeMillis() - start + " number is " + ohlcs.size());


        Ohlc init = new Ohlc(ohlcs.get(0));

        LocalDateTime nextTime = interval.ceilTime(ohlcs.get(0).dateTime);

        List<Ohlc> ret  = new ArrayList<>();

        for(int i = 0; i < ohlcs.size(); i++){
            Ohlc ohlc = ohlcs.get(i);
            if(ohlc.dateTime.isAfter(nextTime)){
                nextTime = interpolateTillEnd(interval, init, nextTime, ret, ohlc.dateTime);
                init = ohlc;
            }
            init = init.merge(ohlc);
        }

        return ret;
    }


    private LocalDateTime interpolateTillEnd(Interval interval, Ohlc init, LocalDateTime nextTime, List<Ohlc> ret, LocalDateTime currTime) {
        while (currTime.isAfter(nextTime)){
            ret.add(init.withTime(nextTime));
            nextTime = nextTime.plus(interval.duration());
        }
        return nextTime;
    }


}
