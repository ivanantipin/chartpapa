package com.iaa.finam;

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
