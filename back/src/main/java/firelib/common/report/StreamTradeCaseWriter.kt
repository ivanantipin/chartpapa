package firelib.common.report

import com.funstat.store.MdDao
import com.funstat.store.SqlUtils
import firelib.common.Order
import firelib.common.Trade
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.domain.Ohlc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.nio.file.Path

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

    fun init(secName: String): String {
        val header = getHeader(tradeCaseColDefs) + factors.associateBy({ it }, { "DOUBLE PRECISION" })
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(secName, header))
        return makeSqlStatementFromHeader(secName, header)

    }


    fun insertTrades(trades: List<Trade>): Unit {
        trades.groupBy { it.security() }.forEach { (sec, secTrades) ->
            val stmt = init(sec)
            val gen = StreamTradeCaseGenerator()
            val cases = secTrades.flatMap(gen).map { toMapForSqlUpdate(it, tradeCaseColDefs) }.toTypedArray()
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
        }
    }
}

class StreamOrderWriter(val path: Path) {
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    private val stmt: String

    init {
        val header = getHeader(orderColsDefs)
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader("orders", header))
        stmt = makeSqlStatementFromHeader("orders", header)
    }

    fun insertOrders(orders: List<Order>): Unit {
        val cases = orders.map { toMapForSqlUpdate(it, orderColsDefs) }.toTypedArray()
        NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
    }
}


class OhlcStreamWriter(val path: Path) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val mdDao = MdDao(ds)


    fun insertOhlcs(secName : String, ohlcs: List<Ohlc>): Unit {
        mdDao.insertOhlc(ohlcs,secName)
    }
}