package com.funstat.domain;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class Label {
    String text;
    LocalDateTime time;
    double level;
    String color;
    boolean drawOnTop;

    public Label(String text, LocalDateTime time, double level, String color,
                 boolean drawOnTop
    ) {
        this.text = text;
        this.time = time;
        this.level = level;
        this.color = color;
        this.drawOnTop = drawOnTop;
    }


    @ApiModelProperty(required = true)
    public String getText() {
        return text;
    }

    @ApiModelProperty(required = true)
    public LocalDateTime getTime() {
        return time;
    }

    @ApiModelProperty(required = true)
    public double getLevel() {
        return level;
    }

    @ApiModelProperty(required = true)
    public String getColor() {
        return color;
    }

    @ApiModelProperty(required = true)
    public boolean isDrawOnTop() {
        return drawOnTop;
    }
}
