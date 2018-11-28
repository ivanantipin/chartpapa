package com.funstat.domain;

import io.swagger.annotations.ApiModelProperty;

public class InstrId {
    @ApiModelProperty(required = true)
    public String id;
    @ApiModelProperty(required = true)
    public String name;
    @ApiModelProperty(required = true)
    public String market;
    @ApiModelProperty(required = true)
    public String code;
    @ApiModelProperty(required = true)
    public String source;

    public InstrId(String id, String name, String market, String code, String source) {
        this.id = id;
        this.name = name;
        this.market = market;
        this.code = code;
        this.source = source;
    }

    @ApiModelProperty(required = true)
    public String getId() {
        return id;
    }

    @ApiModelProperty(required = true)
    public String getName() {
        return name;
    }

    @ApiModelProperty(required = true)
    public String getMarket() {
        return market;
    }

    @ApiModelProperty(required = true)
    public String getCode() {
        return code;
    }

    @ApiModelProperty(required = true)
    public String getSource() {
        return source;
    }

    public InstrId() {
        this(null,null,null,null, null);
    }

    @Override
    public String toString() {
        return "InstrId{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", market='" + market + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
