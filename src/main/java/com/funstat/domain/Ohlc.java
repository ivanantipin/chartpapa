package com.funstat.domain;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Ohlc implements Comparable<Ohlc>{
    public final LocalDateTime dateTime;
    public final double open;
    public final double close;
    public final double high;
    public final double low;

    public Ohlc(LocalDateTime dateTime, double open, double high, double low, double close) {
        this.dateTime = dateTime;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }

    public static Optional<Ohlc> parse(String str){
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
        try {
            String[] arr = str.split(";");
            return Optional.of(new Ohlc(LocalDateTime.parse(arr[0] + " " + arr[1],pattern),
                    Double.parseDouble(arr[2]),
                    Double.parseDouble(arr[3]), Double.parseDouble(arr[4]), Double.parseDouble(arr[5])
            ));
        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "Ohlc{" +
                "dateTime=" + dateTime +
                ", open=" + open +
                ", close=" + close +
                ", high=" + high +
                ", low=" + low +
                '}';
    }

    @Override
    public int compareTo(Ohlc ohlc) {
        return this.dateTime.compareTo(ohlc.dateTime);
    }


    @ApiModelProperty(required = true)
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @ApiModelProperty(required = true)
    public double getOpen() {
        return open;
    }

    @ApiModelProperty(required = true)
    public double getClose() {
        return close;
    }

    @ApiModelProperty(required = true)
    public double getHigh() {
        return high;
    }

    @ApiModelProperty(required = true)
    public double getLow() {
        return low;
    }

    @ApiModelProperty(required = true)
    public double getVolume() {
        return 0;
    }

}

