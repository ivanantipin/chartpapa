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
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.sqlite.SQLiteDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        new JdbcTemplate(ds).execute("create table if not exists " + code + " (dt datetime primary key , o number,h number,l number ,c number) ");
        return new JdbcTemplate(ds).query("select * from " + code, (rs, rowNum) -> {
            return new Ohlc(rs.getTimestamp("DT").toLocalDateTime(), rs.getDouble("o"), rs.getDouble("h"), rs.getDouble("l"), rs.getDouble("c"));
        });
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

    private void saveInTransaction(String sql, Map[] data) {
        DataSourceTransactionManager manager = new DataSourceTransactionManager(ds);
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


    public static void main(String[] args) {
        Symbol symbol = new Symbol("some", "name", "market", "code");
        MdDao dao = new MdDao();
        dao.saveGeneric("test", Collections.singletonList(symbol), s->s.id);
        System.out.println(dao.read("test", Symbol.class));




    }

}
