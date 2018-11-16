package com.funstat.domain;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Label {
    final double level;

    private LocalDateTime time;
    final Map<String, String> attributes;

    public Label(LocalDateTime time, double level, Map<String, String> attributes) {
        this.time = time;
        this.attributes = attributes;
        this.level = level;
    }

    public Label(double level, LocalDateTime time) {
        this.level = level;
        this.time = time;
        attributes = new HashMap<>();
    }

    @ApiModelProperty(required = true)
    public double getLevel() {
        return level;
    }

    @ApiModelProperty(required = true)
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @ApiModelProperty(required = true)
    public LocalDateTime getTime() {
        return time;
    }

    public Label withAttribute(String key, String obj){
        HashMap<String, String> attrs = new HashMap<>(this.attributes);
        attrs.put(key,obj);
        return new Label(this.time, this.level, attrs);
    }


}
