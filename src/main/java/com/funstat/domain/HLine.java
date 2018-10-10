package com.funstat.domain;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class HLine{
    LocalDateTime start;
    LocalDateTime end;
    double level;

    public HLine(LocalDateTime start, LocalDateTime end, double level) {
        this.start = start;
        this.end = end;
        this.level = level;
    }

    public HLine copyWithNewEnd(LocalDateTime end){
        return new HLine(this.start,end,level);
    }

    @ApiModelProperty(required = true)
    public LocalDateTime getStart() {
        return start;
    }

    @ApiModelProperty(required = true)
    public LocalDateTime getEnd() {
        return end;
    }

    @ApiModelProperty(required = true)
    public double getLevel() {
        return level;
    }
}
