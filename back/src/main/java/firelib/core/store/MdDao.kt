package firelib.core.store

import firelib.core.domain.Ohlc
import firelib.core.misc.SqlUtils
import firelib.core.misc.toInstantDefault
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.sqlite.SQLiteDataSource
import org.sqlite.core.CoreStatement
import org.sqlite.core.DB
import org.sqlite.jdbc3.JDBC3ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

class MdDao(internal val ds: SQLiteDataSource) {

    private val manager: DataSourceTransactionManager = DataSourceTransactionManager(ds)

    val log = LoggerFactory.getLogger(javaClass)
    val tableCreated = ConcurrentHashMap<String, Boolean>()

    private fun saveInTransaction(sql: String, data: List<Map<String, Any>>) {
        TransactionTemplate(manager).execute<Any> {
            NamedParameterJdbcTemplate(ds).batchUpdate(sql, data.toTypedArray())
            null
        }
    }

    fun listAvailableInstruments(): List<String> {
        return JdbcTemplate(ds).queryForList(
            """SELECT    name
                FROM
                sqlite_master  
                WHERE  
                    type ='table' AND  
                    name NOT LIKE 'sqlite_%' """, String::class.java
        )!!
    }

    fun deleteSince(codeIn: String, fromTime: Instant) {
        val code = normName(codeIn)
        ensureExist(code)
        ds.connection.use {
            val stmt = it.prepareStatement("delete from $code where dt > ? ", 1)
            stmt.setLong(1, fromTime.toEpochMilli())
            stmt.use {
                it.execute()
            }
        }
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
            val timeSpent = measureTimeMillis {
                saveInTransaction(
                    "insert or replace into $table(DT,O,H,L,C,V) values (:DT,:OPEN,:HIGH,:LOW,:CLOSE,:VOLUME)",
                    data
                )
            }
            log.info("spent ${timeSpent / 1000.0} s. to insert ${ohlcs.size}  into ${table} last time is ${ohlcs.lastOrNull()?.endTime}")
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

    fun normName(name: String): String {
        return name.replace('-', '_').replace('.', '_')
            .replace('@', '_').replace('#', '_')
    }

    fun queryLast(codeIn: String): Ohlc? {
        val code = normName(codeIn)
        ensureExist(code)
        return sequence {
            ds.connection.use {
                val stmt = it.prepareStatement("select * from $code order by dt desc LIMIT 1 ")
                stmt.use {
                    val rs = it.executeQuery()
                    val sqLiteRs = rs as JDBC3ResultSet
                    val stmt1 = sqLiteRs.statement as CoreStatement
                    val db = stmt1.datbase
                    rs.use {
                        while (rs.next()) {
                            // highly optimized code
                            yield(
                                mapOh(db, stmt1)
                            )
                        }
                    }
                }
            }
        }.firstOrNull()
    }

    fun queryAll(codeIn: String): List<Ohlc> {
        return queryAll(codeIn, LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)).toList()
    }

    fun queryAll(codeIn: String, start: LocalDateTime): Sequence<Ohlc> {
        val code = normName(codeIn)

        if (!SqlUtils.checkTableExists(ds, code)) {
            println("table does not exists!!!!! ${code}")
            return emptySequence()
        }

        return sequence {
            ds.connection.use {
                val stmt = it.prepareStatement("select * from $code where dt > ? order by dt asc")
                stmt.use {
                    stmt.setLong(1, start.toInstantDefault().toEpochMilli())
                    val rs = stmt.executeQuery()
                    val sqLiteRs = rs as JDBC3ResultSet
                    val stmt1 = sqLiteRs.statement as CoreStatement
                    val db = stmt1.datbase
                    rs.use {
                        while (rs.next()) {
                            // highly optimized code
                            yield(
                                mapOh(db, stmt1)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun mapOh(db: DB, stmt1: CoreStatement) = Ohlc(
        Instant.ofEpochMilli(db.column_long(stmt1.pointer, 0)),
        db.column_double(stmt1.pointer, 1),
        db.column_double(stmt1.pointer, 2),
        db.column_double(stmt1.pointer, 3),
        db.column_double(stmt1.pointer, 4),
        volume = db.column_long(stmt1.pointer, 5),
        interpolated = false
    )
}
