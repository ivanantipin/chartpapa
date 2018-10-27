package com.funstat.finam;

import io.swagger.annotations.ApiModelProperty;

public class Symbol{
    public final String id;
    public final String name;
    public final String market;
    public final String code;
    public final String source;

    public Symbol(String id, String name, String market, String code, String source) {
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

    public Symbol() {
        this(null,null,null,null, null);
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", market='" + market + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
