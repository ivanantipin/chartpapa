package com.iaa.finam;

import com.funstat.domain.Ohlc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.sqlite.SQLiteDataSource;

import java.util.List;
import java.util.Map;

public class MdDao {

    SQLiteDataSource ds = new SQLiteDataSource();

    {
        ds.setUrl("jdbc:sqlite:/home/ivan/site.db");
    }


    void save(String code, Map[] data) {
        new NamedParameterJdbcTemplate(ds).batchUpdate("insert into " + code + " (DT,O,H,L,C) values (:DT,:OPEN,:HIGH,:LOW,:CLOSE)", data);
    }


    public List<Ohlc> queryAll(String code) {
        new JdbcTemplate(ds).execute("create table if not exists " + code + " (dt datetime primary key , o number,h number,l number ,c number) ");
        return new JdbcTemplate(ds).query("select * from " + code, (RowMapper<Ohlc>) (rs, rowNum) -> {
            return new Ohlc(rs.getTimestamp("DT").toLocalDateTime(), rs.getDouble("o"), rs.getDouble("h"), rs.getDouble("l"), rs.getDouble("c"));
        });
    }

}
