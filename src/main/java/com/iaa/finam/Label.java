package com.iaa.finam;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class Label{
    String text;
    LocalDateTime time;
    boolean high;

    public Label(String text, LocalDateTime time, boolean high) {
        this.text = text;
        this.time = time;
        this.high = high;
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
    public boolean isHigh() {
        return high;
    }
}
