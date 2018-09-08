package com.iaa.finam;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MergedSeries {

    static {
        System.out.println("aoeuaoeuaoeu");
    }

    LocalDateTime[] index;
    Map<String, double[]> closes;

    public MergedSeries(LocalDateTime[] index, Map<String, double[]> closes) {
        this.index = index;
        this.closes = closes;
    }

    public MergedSeries() {
    }


    @ApiModelProperty(required = true)
    public LocalDateTime getUUU(){
        return LocalDateTime.now();
    }


    @ApiModelProperty(required = true )
    public LocalDateTime[] getIndex() {
        return  index;//Arrays.stream(index).map(ld->new Date()).toArray(zs->new Date[zs]);
    }

    @ApiModelProperty(required = true)
    public Map<String, double[]> getCloses() {
        return closes;
    }
}
