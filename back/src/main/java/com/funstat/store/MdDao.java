package com.funstat.store;

import com.funstat.domain.Ohlc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.sqlite.SQLiteDataSource;

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
import java.util.stream.Collectors;

public class MdDao {

    private final DataSourceTransactionManager manager;
    final SQLiteDataSource ds;

    ConcurrentHashMap<String, Boolean> tableCreated = new ConcurrentHashMap<>();

    public MdDao(SQLiteDataSource ds) {
        this.ds = ds;
        this.manager = new DataSourceTransactionManager(ds);
    }


    private void saveInTransaction(String sql, List<Map<String, Object>> data) {
        NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(ds);
        new TransactionTemplate(manager).execute(status -> {
            long start = System.currentTimeMillis();
            namedTemplate.batchUpdate(sql, data.toArray(new Map[0]));
            double dur = (System.currentTimeMillis() - start) / 1000.0;
            System.out.println("inserting " + data.size() + " took " + dur + " sec ," + " rate is " +
                    data.size() / dur + " per sec");
            return null;
        });
    }


    public void insert(List<firelib.domain.Ohlc> ohlcs, String table) {
        ensureExist(table);
        List<Map<String, Object>> data = ohlcs.stream().map(oh -> {
            return new HashMap<String, Object>() {
                {
                    put("DT", Timestamp.valueOf(LocalDateTime.ofInstant(oh.getDtGmtEnd(), ZoneOffset.UTC)));
                    put("OPEN", oh.getO());
                    put("HIGH", oh.getH());
                    put("LOW", oh.getL());
                    put("CLOSE", oh.getC());
                }
            };
        }).collect(Collectors.toList());

        try {
            saveInTransaction("insert into " + table + "(DT,O,H,L,C) values (:DT,:OPEN,:HIGH,:LOW,:CLOSE)", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertJ(List<Ohlc> ohlcs, String table) {
        List<firelib.domain.Ohlc> conv = ohlcs.stream().map(oh -> {
            return new firelib.domain.Ohlc(oh.close, oh.dateTime.toInstant(ZoneOffset.UTC), oh.high, oh.low, oh.open, 0,0);
        }).collect(Collectors.toList());
        insert(conv,table);
    }

    void ensureExist(String table) {
        tableCreated.computeIfAbsent(table, (k) -> {
            JdbcTemplate template = new JdbcTemplate(ds);
            template
                    .execute("create table if not exists " + table +
                            " (dt TIMESTAMPTZ, " +
                            "o DOUBLE PRECISION  not NULL," +
                            "h DOUBLE PRECISION  not NULL," +
                            "l DOUBLE PRECISION  not NULL," +
                            "c DOUBLE PRECISION  not NULL," +
                            "primary key (dt)) ;");

            return true;
        });
    }

    private Ohlc mapOhlc(ResultSet rs) throws SQLException {
        return new Ohlc(rs.getTimestamp("DT").toLocalDateTime(), rs.getDouble("o"), rs.getDouble("h"), rs.getDouble("l"), rs.getDouble("c"));
    }

    public Optional<Ohlc> queryLast(String code) {
        List<Ohlc> ret = new NamedParameterJdbcTemplate(ds).query("select * from " + code + " order by dt desc LIMIT 1 ", (rs, rowNum) -> mapOhlc(rs));
        return ret.size() == 0 ? Optional.empty() : Optional.of(ret.get(0));
    }

    public List<Ohlc> queryAll(String code) {
        return new NamedParameterJdbcTemplate(ds).query("select * from " + code + " order by dt asc ", (rs, rowNum) -> mapOhlc(rs));
    }

}
