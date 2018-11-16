package com.funstat;

public class Pair{
    public final String key;
    public final String value;

    public Pair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Pair() {
        this(null,null);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
