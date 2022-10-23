package firelib.core.store

import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import firelib.core.misc.SqlUtils
import firelib.core.misc.toInstantDefault
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.sqlite.SQLiteDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

val executors = ConcurrentHashMap<String, ExecutorService>()

fun <T> MdDao.updateInThread(block: () -> T) {

    val exec = executors.computeIfAbsent(this.ds.url, {
        Executors.newSingleThreadExecutor({
            Thread(it).apply { isDaemon = true }
        })
    })

    try {
        exec.submit(block).get()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

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
        )
    }

    fun truncate(instrId: InstrId) {
        updateInThread {
            val table = MdStorageImpl.makeTableName(instrId)
            ds.connection.use {
                val stmt = it.prepareStatement("delete from $table ")
                stmt.use {
                    it.execute()
                }
            }
        }
    }

    fun deleteSince(codeIn: String, fromTime: Instant) {
        updateInThread {
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
    }

    fun insertOhlc(ohlcs: List<Ohlc>, instrId: InstrId) {
        insertOhlc(ohlcs, MdStorageImpl.makeTableName(instrId))
    }

    fun insertOhlc(ohlcs: List<Ohlc>, tableIn: String) {
        updateInThread {
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
                if (timeSpent > 1000) {
                    log.info("spent ${timeSpent / 1000.0} s. to insert ${ohlcs.size}  into ${table} last time is ${ohlcs.lastOrNull()?.endTime}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            .replace('@', '_').replace('#', '_').replace(':', '_')
    }

    fun queryLast(instrId: InstrId): Ohlc? {
        return queryLast(MdStorageImpl.makeTableName(instrId))
    }

    fun queryLast(codeIn: String): Ohlc? {
        val code = normName(codeIn)
        ensureExist(code)
        return queryToSequence { it.prepareStatement("select * from $code order by dt desc LIMIT 1 ") }.firstOrNull()
    }

    fun queryPoint(codeIn: String, epochMs: Long): Ohlc? {
        val code = normName(codeIn)
        ensureExist(code)
        return queryToSequence {
            val stmt = it.prepareStatement("select * from $code where dt < ? order by dt desc LIMIT 1 ")
            stmt.setLong(1, epochMs)
            stmt

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

        return queryToSequence({
            val stmt = it.prepareStatement("select * from $code where dt > ? order by dt asc")
            stmt.setLong(1, start.toInstantDefault().toEpochMilli())
            stmt
        })
    }

    private fun queryToSequence(
        prepStatement: (Connection) -> PreparedStatement
    ) = sequence {
        ds.connection.use {
            val stmt = prepStatement(it)
            stmt.use {
                val rs = stmt.executeQuery()
                while (rs.next()){
                    val oh = Ohlc(
                        Instant.ofEpochMilli(rs.getLong(1)),
                        rs.getDouble(2),
                        rs.getDouble(3),
                        rs.getDouble(4),
                        rs.getDouble(5),
                        volume = rs.getLong(6),
                        interpolated = false
                    )
                    yield(oh)
                }
            }
        }
    }

}

