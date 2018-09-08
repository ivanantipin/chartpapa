package com.iaa.finam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Ohlc implements Comparable<Ohlc>{
    public final LocalDateTime time;
    public final double open;
    public final double close;
    public final double high;
    public final double low;

    public Ohlc(LocalDateTime time, double open, double high, double low, double close) {
        this.time = time;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }

    static Optional<Ohlc> parse(String str){
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
        try {
            String[] arr = str.split(";");
            return Optional.of(new Ohlc(LocalDateTime.parse(arr[0] + " " + arr[1],pattern),
                    Double.parseDouble(arr[1]),
                    Double.parseDouble(arr[2]), Double.parseDouble(arr[3]), Double.parseDouble(arr[4])
            ));
        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "Ohlc{" +
                "time=" + time +
                ", open=" + open +
                ", close=" + close +
                ", high=" + high +
                ", low=" + low +
                '}';
    }

    @Override
    public int compareTo(Ohlc ohlc) {
        return this.time.compareTo(ohlc.time);
    }
}
