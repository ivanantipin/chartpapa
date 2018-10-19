package com.funstat.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funstat.domain.Ohlc;
import com.funstat.finam.FinamDownloader;
import com.funstat.finam.Symbol;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.sqlite.SQLiteDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MdDao {

    SQLiteDataSource ds = new SQLiteDataSource();

    {
        ds.setUrl("jdbc:sqlite:/home/ivan/site.db");
    }


    public void save(String code, Map[] data) {
        saveInTransaction("insert OR REPLACE into " + code + " (DT,O,H,L,C) values (:DT,:OPEN,:HIGH,:LOW,:CLOSE)", data);
    }


    public List<Ohlc> queryAll(String code) {
        ensureTableExists(code);
        return new JdbcTemplate(ds).query("select * from " + code + " order by dt asc ", (rs, rowNum) -> mapOhlc(rs));
    }

    private void ensureTableExists(String code) {
        new JdbcTemplate(ds).execute("create table if not exists " + code + " (dt datetime primary key , o number,h number,l number ,c number) ");
    }

    private Ohlc mapOhlc(ResultSet rs) throws SQLException {
        return new Ohlc(rs.getTimestamp("DT").toLocalDateTime(), rs.getDouble("o"), rs.getDouble("h"), rs.getDouble("l"), rs.getDouble("c"));
    }

    public Optional<Ohlc> queryLast(String code){
        ensureTableExists(code);
        List<Ohlc> ret = new JdbcTemplate(ds).query("select * from " + code + " order by dt desc LIMIT 1 ", (rs, rowNum) -> mapOhlc(rs));
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

        new JdbcTemplate(ds).execute("create table if not exists " + type + " (json varchar, key varchar primary key )");


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

    DataSourceTransactionManager manager = new DataSourceTransactionManager(ds);

    private void saveInTransaction(String sql, Map[] data) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.execute(status -> {
            new NamedParameterJdbcTemplate(ds).batchUpdate(sql, data);
            return null;
        });
    }

    public <T> List<T> read(String type, Class<T> clazz){
        return new JdbcTemplate(ds).query("select * from " + type, (rs, rowNum) -> {
            return deser(rs.getString("json"), clazz);
        });
    }



}
