package com.funstat.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funstat.domain.Ohlc;
import com.google.common.collect.Lists;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenericDaoImpl implements GenericDao {

    private final DataSourceTransactionManager manager;
    final SQLiteDataSource ds;

    public GenericDaoImpl(SQLiteDataSource ds) {
        this.ds = ds;
        this.manager = new DataSourceTransactionManager(ds);
    }

    private HashMap<String, Object> getParamsMap(String code) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("CODE", code);
        return params;
    }

    String write(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream str = new ByteArrayOutputStream();
        try {
            mapper.writer().writeValue(str, obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(str.toByteArray());

    }

    <T> T deser(String str, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(str, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized <T> void saveGeneric(String type, List<T> obj, Function<T, String> keyMapper) {

        ensureExistGeneric(type);

        System.out.println("inserting " + obj.size() + " into " + type);

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


    private void saveInTransaction(String sql, Map[] data) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.execute(status -> {
            new NamedParameterJdbcTemplate(ds).batchUpdate(sql, data);
            return null;
        });
    }

    @Override
    public synchronized <T> List<T> readGeneric(String tableName, Class<T> clazz) {
        ensureExistGeneric(tableName);
        return new JdbcTemplate(ds).query("select * from " + tableName, (rs, rowNum) -> {
            return deser(rs.getString("json"), clazz);
        });
    }
}
