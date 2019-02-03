package firelib.common.report

import com.funstat.store.MdDao
import com.funstat.store.SqlUtils
import firelib.common.Order
import firelib.common.Trade
import firelib.common.misc.StreamTradeCaseGenerator
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
    val dumper = GenericDumper("test", Paths.get("test.db"), Test::class)
    dumper.write(listOf(Test("N0", Instant.now()), Test("N1", Instant.now())))
}
