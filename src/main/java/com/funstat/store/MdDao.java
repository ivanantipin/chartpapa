package com.funstat.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funstat.domain.Ohlc;
import com.funstat.finam.Symbol;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.sqlite.SQLiteDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class MdDao {

    SQLiteDataSource ds = new SQLiteDataSource();

    {
        ds.setUrl("jdbc:sqlite:/home/ivan/site.db");
        new JdbcTemplate(ds).execute("create table if not exists ohlc (source varchar, code varchar, dt datetime, o number,h number,l number ,c number, primary key (source,code,dt)) ");
    }

    public void saveOhlc(Symbol symbol, List<Ohlc> data) {
        Map[] maps = data.stream().map(oh -> {
            return new HashMap<String, Object>() {
                {
                    put("SOURCE",symbol.source);
                    put("CODE",symbol.code);
                    put("DT", Timestamp.valueOf(oh.dateTime));
                    put("OPEN", oh.open);
                    put("HIGH", oh.high);
                    put("LOW", oh.low);
                    put("CLOSE", oh.close);
                }
            };
        }).toArray(sz -> new Map[sz]);
        System.out.println("to be inserted/updated " + maps.length + " into " + symbol.code);
        try {
            saveInTransaction("insert OR REPLACE into ohlc (SOURCE, CODE, DT,O,H,L,C) values (:SOURCE,:CODE,:DT,:OPEN,:HIGH,:LOW,:CLOSE)", maps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("DONE " + symbol.code);
    }


    public List<Ohlc> queryAll(String code, String source) {
        HashMap<String, Object> params = getParamsMap(code, source);
        return new NamedParameterJdbcTemplate(ds).query("select * from ohlc where code=:CODE and source=:SOURCE order by dt asc ", params, (rs, rowNum) -> mapOhlc(rs));
    }

    private HashMap<String, Object> getParamsMap(String code, String source) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("CODE",code);
        params.put("SOURCE",source);
        return params;
    }


    private Ohlc mapOhlc(ResultSet rs) throws SQLException {
        return new Ohlc(rs.getTimestamp("DT").toLocalDateTime(), rs.getDouble("o"), rs.getDouble("h"), rs.getDouble("l"), rs.getDouble("c"));
    }

    public Optional<Ohlc> queryLast(String code, String source){
        List<Ohlc> ret = new NamedParameterJdbcTemplate(ds).query("select * from ohlc where source = :SOURCE and code=:CODE order by dt desc LIMIT 1 ", getParamsMap(code,source), (rs, rowNum) -> mapOhlc(rs));
        return ret.size() == 0 ? Optional.empty() : Optional.of(ret.get(0));
    }

    String write(Object obj){
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream str = new ByteArrayOutputStream();
        try {
            mapper.writer().writeValue(str, obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(str.toByteArray());

    }

    <T> T deser(String str, Class<T> clazz){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(str,clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void saveGeneric(String type, List<T> obj, Function<T,String> keyMapper){

        ensureExistGeneric(type);


        System.out.println("inserting " + obj.size() + " into " + type);

        AtomicInteger cnt = new AtomicInteger();

        HashMap[] data = obj.stream().map(ob -> {
            return new HashMap() {
                {
                    put("KEY", keyMapper.apply(ob));
                    put("JSON", write(ob));
                }
            };
        }).toArray(i -> new HashMap[i]);

        saveInTransaction("insert or replace into " + type + " (key,json) values (:KEY,:JSON)", data);

    }

    private void ensureExistGeneric(String type) {
        new JdbcTemplate(ds).execute("create table if not exists " + type + " (json varchar, key varchar primary key )");
    }

    DataSourceTransactionManager manager = new DataSourceTransactionManager(ds);

    private void saveInTransaction(String sql, Map[] data) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.execute(status -> {
            new NamedParameterJdbcTemplate(ds).batchUpdate(sql, data);
            return null;
        });
    }

    public <T> List<T> readGeneric(String tableName, Class<T> clazz){
        ensureExistGeneric(tableName);
        return new JdbcTemplate(ds).query("select * from " + tableName, (rs, rowNum) -> {
            return deser(rs.getString("json"), clazz);
        });
    }



}
