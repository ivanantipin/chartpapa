package firelib.common.report

import com.funstat.store.MdDao
import com.funstat.store.SqlUtils
import firelib.common.Order
import firelib.common.Trade
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.common.misc.dbl2Str
import firelib.common.misc.writeRows
import firelib.common.opt.OptimizedParameter
import firelib.domain.Ohlc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

fun makeSqlStatementFromHeader(table: String, header: Map<String, String>): String {
    val names = header.toList().map { it.first }
    val decl = names.joinToString(separator = ",")
    val vals = names.joinToString(separator = ",") { ":${it}" }
    return "insert into $table ($decl) values ( ${vals} )"
}

fun makeCreateSqlStmtFromHeader(table: String, header: Map<String, String>): String {
    val t0 = header.toList().map { "${it.first} ${it.second} not NULL" }.joinToString(separator = ",")
    return "create table if not exists $table ( ${t0} ) ;"
}

class StreamTradeCaseWriter(val path: Path, val factors: Iterable<String>) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val stmt : String
    val tman = DataSourceTransactionManager(ds)

    init {
        val header = getHeader(tradeCaseColDefs) + factors.associateBy({ it }, { "DOUBLE PRECISION" })
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader("trades", header))
        this.stmt = makeSqlStatementFromHeader("trades", header)
    }

    fun insertTrades(trades: List<Trade>): Unit {
        GlobalLock.lock.withLock {
            TransactionTemplate(tman).execute({ status ->
                trades.groupBy { it.security() }.forEach { (sec, secTrades) ->
                    val gen = StreamTradeCaseGenerator()
                    val cases = secTrades.flatMap(gen).map { toMapForSqlUpdate(it, tradeCaseColDefs) }.toTypedArray()
                    NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
                }
            })
        }
    }
}

class StreamOrderWriter(val path: Path) {
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val tman = DataSourceTransactionManager(ds)
    private val stmt: String

    init {
        val header = getHeader(orderColsDefs)
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader("orders", header))
        stmt = makeSqlStatementFromHeader("orders", header)
    }

    fun insertOrders(orders: List<Order>): Unit {
        GlobalLock.lock.withLock {
            TransactionTemplate(tman).execute({ status ->
                val cases = orders.map { toMapForSqlUpdate(it, orderColsDefs) }.toTypedArray()
                NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
            })
        }
    }
}

object GenericMapWriter{
    fun write(path : Path, rows: List<Map<String,Any>>, tableName : String) {

        val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
        val stmt: String

        val colsDef = rows[0].map {
            ColDef<Map<String,Any>, Any>(it.key, { v: Map<String, Any> ->
                v[it.key]!!
            }, it.value::class.createType())
        }.toTypedArray()

        val header = getHeader(colsDef as Array<ColDef<Map<String,Any>, out Any>>)
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(tableName, header))
        stmt = makeSqlStatementFromHeader(tableName, header)
        NamedParameterJdbcTemplate(ds).batchUpdate(stmt, rows.toTypedArray())
    }
}

object OptWriter{

    fun write(path : Path, estimates: List<ExecutionEstimates>) {
        var colsDef : List<ColDef<Map<String,Any>,out Any>>? = null
        val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
        val stmt: String
        val rows = estimates.map {
            it.metricToValue.mapKeys { it.key.name } + it.optParams.mapKeys { "opt_${it.key}" }
        }
        GenericMapWriter.write(path,rows, "opts")
    }
}

object GlobalLock{
    val lock = ReentrantLock()
}



class OhlcStreamWriter(val path: Path) {
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val mdDao = MdDao(ds)
    fun insertOhlcs(secName : String, ohlcs: List<Ohlc>): Unit {
        GlobalLock.lock.withLock {
            mdDao.insertOhlc(ohlcs,secName)
        }

    }
}

class GenericDumper<T : Any>(val name : String, val path : Path, val type : KClass<T>){
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val tman = DataSourceTransactionManager(ds)

    val stmt : String

    init {
        val header = type.memberProperties.associateBy({ it.name }, { SqlTypeMapper.mapType(it.returnType) })
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(name,header))
        stmt = makeSqlStatementFromHeader(name, header)
    }

    fun write(rows : List<T>){
        val rowsM = rows.map { row ->
            type.memberProperties.associateBy({ it.name }, { it.get(row) })
        }
        TransactionTemplate(tman).execute({ status ->
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, rowsM.toTypedArray())
        })
    }
}


data class Test(val name : String, val time : Instant)
fun main(args: Array<String>) {

    OptWriter.write(Paths.get("test.db"), listOf(ExecutionEstimates(mapOf("a" to 1), mapOf(StrategyMetric.AvgLoss to 1.0))))

}
