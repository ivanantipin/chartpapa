package com.funstat.store

import com.funstat.domain.Ohlc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.sqlite.SQLiteDataSource

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.HashMap
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class MdDao(internal val ds: SQLiteDataSource) {

    private val manager: DataSourceTransactionManager

    internal var tableCreated = ConcurrentHashMap<String, Boolean>()

    init {
        this.manager = DataSourceTransactionManager(ds)
    }


    private fun saveInTransaction(sql: String, data: List<Map<String, Any>>) {
        val namedTemplate = NamedParameterJdbcTemplate(ds)
        TransactionTemplate(manager).execute<Any> { status ->
            val start = System.currentTimeMillis()
            namedTemplate.batchUpdate(sql, data.toTypedArray())
            val dur = (System.currentTimeMillis() - start) / 1000.0
            println("inserting " + data.size + " took " + dur + " sec ," + " rate is " +
                    data.size / dur + " per sec")
            null
        }
    }


    fun insert(ohlcs: List<firelib.domain.Ohlc>, table: String) {
        ensureExist(table)
        val data = ohlcs.map {  (dtGmtEnd, O, H, L, C) ->
            object : HashMap<String, Any>() {
                init {
                    put("DT", Timestamp.valueOf(LocalDateTime.ofInstant(dtGmtEnd, ZoneOffset.UTC)))
                    put("OPEN", O)
                    put("HIGH", H)
                    put("LOW", L)
                    put("CLOSE", C)
                }
            }
        }
        try {
            saveInTransaction("insert into $table(DT,O,H,L,C) values (:DT,:OPEN,:HIGH,:LOW,:CLOSE)", data)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun insertJ(ohlcs: List<Ohlc>, table: String) {
        val conv = ohlcs
                .map { (dateTime, open, high, low, close) -> firelib.domain.Ohlc(dateTime.toInstant(ZoneOffset.UTC), open, high, low, close, 0, 0) }
        insert(conv, table)
    }

    internal fun ensureExist(table: String) {
        (tableCreated as java.util.Map<String, Boolean>).computeIfAbsent(table) { k ->
            val template = JdbcTemplate(ds)
            template
                    .execute("create table if not exists " + table +
                            " (dt TIMESTAMPTZ, " +
                            "o DOUBLE PRECISION  not NULL," +
                            "h DOUBLE PRECISION  not NULL," +
                            "l DOUBLE PRECISION  not NULL," +
                            "c DOUBLE PRECISION  not NULL," +
                            "primary key (dt)) ;")

            true
        }
    }

    @Throws(SQLException::class)
    private fun mapOhlc(rs: ResultSet): Ohlc {
        return Ohlc(rs.getTimestamp("DT").toLocalDateTime(), rs.getDouble("o"), rs.getDouble("h"), rs.getDouble("l"), rs.getDouble("c"))
    }

    fun queryLast(code: String): Optional<Ohlc> {
        ensureExist(code)
        val ret = NamedParameterJdbcTemplate(ds).query("select * from $code order by dt desc LIMIT 1 ") { rs, rowNum -> mapOhlc(rs) }
        return if (ret.size == 0) Optional.empty() else Optional.of(ret[0])
    }

    fun queryAll(code: String): List<Ohlc> {
        ensureExist(code)
        return NamedParameterJdbcTemplate(ds).query("select * from $code order by dt asc ") { rs, rowNum -> mapOhlc(rs) }
    }

}
