package com.funstat.domain;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class TimePoint implements Comparable<TimePoint>{
    final LocalDateTime time;
    final double value;

    public TimePoint(LocalDateTime time, double value) {
        this.time = time;
        this.value = value;
    }

    public TimePoint() {
        this.time = LocalDateTime.now();
        this.value = Double.NaN;
    }

    @ApiModelProperty(required = true)
    public LocalDateTime getTime() {
        return time;
    }

    @ApiModelProperty(required = true)
    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(TimePoint timePoint) {
        return this.time.compareTo(timePoint.time);
    }
}