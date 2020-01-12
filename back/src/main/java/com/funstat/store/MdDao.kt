package com.funstat.store

import com.funstat.finam.FinamDownloader
import firelib.common.interval.Interval
import firelib.common.misc.atUtc
import firelib.common.misc.toInstantDefault
import firelib.common.reader.SimplifiedReader
import firelib.domain.Ohlc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.sqlite.SQLiteDataSource
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MdDao(internal val ds: SQLiteDataSource) {

    private val manager: DataSourceTransactionManager = DataSourceTransactionManager(ds)

    internal var tableCreated = ConcurrentHashMap<String, Boolean>()

    private fun saveInTransaction(sql: String, data: List<Map<String, Any>>) {

        TransactionTemplate(manager).execute<Any> {
            val start = System.currentTimeMillis()
            NamedParameterJdbcTemplate(ds).batchUpdate(sql, data.toTypedArray())
            val dur = (System.currentTimeMillis() - start) / 1000.0
            println("MdDao: inserting " + data.size + " took " + dur + " sec ," + " rate is " +
                    data.size / dur + " per sec")
            null
        }
    }


    fun insertOhlc(ohlcs: List<Ohlc>, tableIn: String) {
        val table = normName(tableIn)
        ensureExist(table)
        val data = ohlcs.filter {
            if (it.open.isFinite()) {
                true
            } else {
                println("not correct ${it}")
                false
            }
        }.map {
            mapOf("DT" to it.endTime.toEpochMilli(),
                    "OPEN" to it.open,
                    "HIGH" to it.high,
                    "LOW" to it.low,
                    "CLOSE" to it.close,
                    "VOLUME" to it.volume
            )
        }

        try {
            saveInTransaction("insert or replace into $table(DT,O,H,L,C,V) values (:DT,:OPEN,:HIGH,:LOW,:CLOSE,:VOLUME)", data)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun ensureExist(table: String) {
        tableCreated.computeIfAbsent(table) { k ->
            JdbcTemplate(ds).execute("create table if not exists ${table}"  +
                            " (dt INTEGER not NULL, " +
                            "o DOUBLE PRECISION  not NULL," +
                            "h DOUBLE PRECISION  not NULL," +
                            "l DOUBLE PRECISION  not NULL," +
                            "c DOUBLE PRECISION  not NULL," +
                            "v INT not NULL," +
                            "primary key (dt)) ;")

            true
        }
    }

    private fun mapOhlc(rs: ResultSet): Ohlc {
        return Ohlc(rs.getTimestamp("DT").toInstant(), rs.getDouble("o"), rs.getDouble("h"), rs.getDouble("l"), rs.getDouble("c"), volume = rs.getLong("v"), interpolated = false)
    }

    fun normName(name: String): String {
        return name.replace('-', '_');
    }

    fun queryLast(codeIn: String): Optional<Ohlc> {
        val code = normName(codeIn)
        ensureExist(code)
        val ret = NamedParameterJdbcTemplate(ds).query("select * from $code order by dt desc LIMIT 1 ") { rs, rowNum -> mapOhlc(rs) }
        return if (ret.size == 0) Optional.empty() else Optional.of(ret[0])
    }

    fun queryAll(codeIn: String): List<Ohlc> {
        return queryAll(codeIn, LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC))
    }

    fun queryAll(codeIn: String, start: LocalDateTime, limit : Int = 10_000_000): List<Ohlc> {
        val code = normName(codeIn)
        ensureExist(code)

        val map = mapOf("DT" to  start.toInstantDefault().toEpochMilli(),
                "LIMIT" to limit
        )
        return NamedParameterJdbcTemplate(ds).query("select * from $code where dt > :DT order by dt asc LIMIT :LIMIT", map, { rs, _ -> mapOhlc(rs) })
    }


}

class SimplifiedReaderImpl(val mdDao: MdDao, val code : String, val startTime : Instant) : SimplifiedReader{

    var lastRead : Instant = startTime

    var buffer  = LinkedList<Ohlc>()

    fun read(){
        println("doing thing")
        val list = mdDao.queryAll(code, lastRead.atUtc(), 20_000)
        if(list.isNotEmpty()){
            buffer.addAll(list)
            lastRead = list.last().endTime
        }
    }

    override fun peek(): Ohlc? {
        if(buffer.isEmpty()){
            read()
        }
        return buffer.peek()
    }

    override fun poll(): Ohlc {
        return buffer.poll()
    }

}



fun main(){
    val impl = MdStorageImpl()
    val dao = impl.getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    val start = LocalDateTime.now().minusDays(200).toInstantDefault()
    val reader = SimplifiedReaderImpl(dao, "sber", start)
    while(reader.peek() != null){
        println("ohlc ${reader.poll()}")
    }
}