package com.funstat.iqfeed;

import com.funstat.domain.Ohlc;
import firelib.common.interval.Interval;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IntervalTransformer {

    public static List<Ohlc> transform(Interval interval, List<Ohlc> ohlcs){
        long start = System.currentTimeMillis();
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

    private static LocalDateTime interpolateTillEnd(Interval interval, Ohlc init, LocalDateTime nextTime, List<Ohlc> ret, LocalDateTime currTime) {
        while (currTime.isAfter(nextTime)){
            ret.add(init.withTime(nextTime));
            nextTime = nextTime.plus(interval.duration());
        }
        return nextTime;
    }


}
