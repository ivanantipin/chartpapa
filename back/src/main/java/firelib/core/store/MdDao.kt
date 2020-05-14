package firelib.core.store

import firelib.core.domain.Ohlc
import firelib.core.misc.toInstantDefault
import firelib.core.report.Sqls
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.sqlite.SQLiteDataSource
import org.sqlite.core.CoreStatement
import org.sqlite.jdbc3.JDBC3ResultSet
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class MdDao(internal val ds: SQLiteDataSource) {

    private val manager: DataSourceTransactionManager = DataSourceTransactionManager(ds)

    val log = LoggerFactory.getLogger(javaClass)
    internal var tableCreated = ConcurrentHashMap<String, Boolean>()

    private fun saveInTransaction(sql: String, data: List<Map<String, Any>>) {

        TransactionTemplate(manager).execute<Any> {
            val start = System.currentTimeMillis()
            NamedParameterJdbcTemplate(ds).batchUpdate(sql, data.toTypedArray())
            val dur = (System.currentTimeMillis() - start) / 1000.0
            log.info(
                "MdDao: inserting " + data.size + " took " + dur + " sec ," + " rate is " +
                        data.size / dur + " per sec"
            )
            null
        }
    }

    fun listAvailableInstruments() : List<String>{
        return JdbcTemplate(ds).queryForList("""SELECT    name
                FROM
                sqlite_master  
                WHERE  
                    type ='table' AND  
                    name NOT LIKE 'sqlite_%' """, String::class.java)!!
    }


    fun insertOhlc(ohlcs: List<Ohlc>, tableIn: String) {
        val table = normName(tableIn)
        ensureExist(table)
        val data = ohlcs.filter {
            if (it.open.isFinite()) {
                true
            } else {
                log.info("not correct ${it}")
                false
            }
        }.map {
            mapOf(
                "DT" to it.endTime.toEpochMilli(),
                "OPEN" to it.open,
                "HIGH" to it.high,
                "LOW" to it.low,
                "CLOSE" to it.close,
                "VOLUME" to it.volume
            )
        }

        try {
            saveInTransaction(
                "insert or replace into $table(DT,O,H,L,C,V) values (:DT,:OPEN,:HIGH,:LOW,:CLOSE,:VOLUME)",
                data
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun ensureExist(table: String) {
        tableCreated.computeIfAbsent(table) { k ->
            JdbcTemplate(ds).execute(
                "create table if not exists ${table}" +
                        " (dt INTEGER not NULL, " +
                        "o DOUBLE PRECISION  not NULL," +
                        "h DOUBLE PRECISION  not NULL," +
                        "l DOUBLE PRECISION  not NULL," +
                        "c DOUBLE PRECISION  not NULL," +
                        "v INT not NULL," +
                        "primary key (dt)) ;"
            )

            true
        }
    }

    private fun mapOhlc(rs: ResultSet, expectedSize: Int = 1): List<Ohlc> {

        val sqLiteRs = rs as JDBC3ResultSet

        val stmt = sqLiteRs.statement as CoreStatement

        val db = stmt.datbase

        val ret = ArrayList<Ohlc>(expectedSize)

        while (rs.next()) {
            // highly optimized code
            val oh = Ohlc(
                Instant.ofEpochMilli(db.column_long(stmt.pointer, 0)),
                db.column_double(stmt.pointer, 1),
                db.column_double(stmt.pointer, 2),
                db.column_double(stmt.pointer, 3),
                db.column_double(stmt.pointer, 4),
                volume = db.column_long(stmt.pointer, 5),
                interpolated = false
            )
            ret.add(oh)
        }
        return ret;

    }

    fun normName(name: String): String {
        return name.replace('-', '_').replace('.', '_');
    }

    fun queryLast(codeIn: String): Optional<Ohlc> {
        val code = normName(codeIn)
        ensureExist(code)
        val ret = NamedParameterJdbcTemplate(ds).query(
            "select * from $code order by dt desc LIMIT 1 ",
            object : ResultSetExtractor<List<Ohlc>> {
                override fun extractData(rs: ResultSet): List<Ohlc>? {
                    return mapOhlc(rs)
                }
            })
        return if (ret.size == 0) Optional.empty() else Optional.of(ret[0])
    }

    fun queryAll(codeIn: String): List<Ohlc> {
        return queryAll(codeIn, LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC))
    }

    val existingTables by lazy {
        Sqls.listAllTables(ds).map { it.toLowerCase() }.toSet()
    }

    fun queryAll(codeIn: String, start: LocalDateTime, limit: Int = 10_000_000): List<Ohlc> {
        val code = normName(codeIn)
        if (!existingTables.contains(code.toLowerCase())) {
            println("table does not exists!!!!! ${code}")
            return emptyList()
        }

        val map = mapOf(
            "DT" to start.toInstantDefault().toEpochMilli(),
            "LIMIT" to limit
        )
        return NamedParameterJdbcTemplate(ds).query(
            "select * from $code where dt > :DT order by dt asc LIMIT :LIMIT",
            map, object : ResultSetExtractor<List<Ohlc>> {
                override fun extractData(rs: ResultSet): List<Ohlc> {
                    return mapOhlc(rs, limit)
                }
            }
        )!!
    }
}
