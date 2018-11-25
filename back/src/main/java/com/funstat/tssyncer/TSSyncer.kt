package com.funstat.tssyncer

import com.google.common.collect.Lists
import firelib.domain.Ohlc
import firelib.parser.*
import org.postgresql.ds.PGPoolingDataSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.sqlite.SQLiteDataSource

import javax.sql.DataSource
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors

class TSSyncer {

    private val ds: DataSource
    private val manager: DataSourceTransactionManager

    internal//new JdbcTemplate(ds).execute("create table if not exists ohlc (source varchar, code varchar, dt datetime, o number,h number,l number ,c number, primary key (source,code,dt)) ");
    val liteDs: DataSource
        get() {
            val ds = SQLiteDataSource()

            run { ds.url = "jdbc:sqlite:/ddisk/globaldatabase/liteTemp/toBeRemoved.db" }
            return ds
        }

    init {
        ds = liteDs
        manager = DataSourceTransactionManager(ds)
    }

    internal fun getDs(): DataSource {
        val source = PGPoolingDataSource()
        source.dataSourceName = "A Data Source"
        source.serverName = "localhost"
        source.databaseName = "mddb"
        source.user = "ivan"
        source.password = "MAI28mama10"
        source.sendBufferSize = 10000000
        source.binaryTransfer = true
        //        source.setAutosave(AutoSave.ALWAYS);
        source.defaultRowFetchSize = 50000
        source.preparedStatementCacheQueries = 10
        source.maxConnections = 10
        return source
    }

    internal fun createTable(table: String) {
        val template = JdbcTemplate(ds)
        template
                .execute("create table if not exists " + table +
                        " (dt TIMESTAMPTZ, " +
                        "o DOUBLE PRECISION  not NULL," +
                        "h DOUBLE PRECISION  not NULL," +
                        "l DOUBLE PRECISION  not NULL," +
                        "c DOUBLE PRECISION  not NULL," +
                        "primary key (dt)) ;")

        //template.execute("select create_hypertable('" + table + "' , 'dt');");
    }


    private fun saveInTransaction(sql: String, data: List<Map<String, Any>>) {
        val template = TransactionTemplate(manager)
        template.execute<Any> { status ->
            val start = System.currentTimeMillis()
            val template1 = NamedParameterJdbcTemplate(ds)
            Lists.partition(data, 100000).forEach {
                ll -> template1. batchUpdate(sql, ll.toTypedArray<Map<String, Any>>())
            }
            val dur = (System.currentTimeMillis() - start) / 1000.0
            println("inserting " + data.size + " took " + dur + " sec ," + " rate is " +
                    data.size / dur + " per sec")
            null
        }
    }

    fun readTable(table: String): List<Ohlc> {
        return JdbcTemplate(ds).query("select * from $table order by dt") { r, num ->
            val ts = r.getTimestamp("dt")
            Ohlc(O=  r.getDouble("o"),H = r.getDouble("h"), L= r.getDouble("l"),
                    C= r.getDouble("C"), dtGmtEnd = ts.toInstant())
        }
    }


    fun insert(ohlcs: List<Ohlc>, table: String) {
        val data = ohlcs.map { (C, dtGmtEnd, H, L, O) ->
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

    companion object {

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {


            val syncer = TSSyncer()


            /*
        long start = System.currentTimeMillis();
        List<Ohlc> list = syncer.readTable("aapl");

        System.out.println("size is " + list.size() + " in " + (System.currentTimeMillis() - start));
*/


            ssync(syncer)

            /*




        parser.seek(Instant.MIN);




        while (parser.read()){

            Ohlc oh = parser.current();



        }
*/


        }

        private fun ssync(syncer: TSSyncer) {
            val dir = Paths.get("/ddisk/globaldatabase/1MIN/STK")

            val cnt = AtomicLong()

            val iniFile = dir.resolve("common.ini").toAbsolutePath().toString()


            val load = LegacyMarketDataFormatLoader.load(iniFile)
            val producer = ParserHandlersProducer(load)


            //val ret: CsvParser[T] = new CsvParser[T](path.toAbsolutePath.toString, generator.handlers.asInstanceOf[Array[ParseHandler[T]]], factory)


            for (s in dir.toFile().list { f, b -> b.endsWith("csv") }!!) {
                try {
                    val table = s.replace(".csv".toRegex(), "").replace("_1".toRegex(), "")
                    syncer.createTable(table)
                    val fname = dir.resolve(s).toAbsolutePath().toString()
                    val ohlcs = ArrayList<Ohlc>()
                    try {
                        val parser = CsvParser<Ohlc>(fname, producer.handlers as Array<out ParseHandler<Ohlc>>?, { Ohlc() }, 100000000)
                        println(parser.seek(Instant.MIN))


                        while (parser.read()) {

                            ohlcs.add(parser.current())
                            cnt.incrementAndGet()

                        }

                        syncer.insert(ohlcs, table)

                        println("done $fname cnt is $cnt")

                    } catch (e: Exception) {
                        println("failed " + s + " due to " + e.message)
                        e.printStackTrace()
                    }

                } catch (e: Exception) {
                    println("failed for " + s + " due to " + e.message)
                }

            }
        }
    }
}
