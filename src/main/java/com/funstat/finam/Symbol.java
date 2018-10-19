package com.funstat.finam;

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

    public String tableToPersist(){
        return code.replace('.','_');
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
