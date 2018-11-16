package com.funstat.domain;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HLine{
    LocalDateTime start;
    LocalDateTime end;
    double level;
    Map<String,String> attributes = new HashMap<>();


    public HLine(LocalDateTime start, LocalDateTime end, double level, Map<String,String> attributes) {
        this.start = start;
        this.end = end;
        this.level = level;
        this.attributes = attributes;
    }

    public HLine(LocalDateTime start, LocalDateTime end, double level) {
        this(start,end,level,new HashMap<>());
    }

    public HLine withAttribute(String name, String value){
        HashMap<String, String> attrs = new HashMap<>(attributes);
        attrs.put(name,value);
        return new HLine(this.start,this.end,this.level, attrs);
    }


    public HLine copyWithNewEnd(LocalDateTime end){
        return new HLine(this.start,end,level,attributes);
    }

    @ApiModelProperty(required = true)
    public LocalDateTime getStart() {
        return start;
    }

    @ApiModelProperty(required = true)
    public Map<String,String> getAttributes() {
        return attributes;
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
