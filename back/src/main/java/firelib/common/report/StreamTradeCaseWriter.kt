package firelib.common.report

import com.funstat.store.MdDao
import firelib.common.Order
import firelib.common.Trade
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.common.misc.toTradingCases
import firelib.common.report.SqlUtils.makeCreateSqlStmtFromHeader
import firelib.common.report.SqlUtils.makeSqlStatementFromHeader
import firelib.domain.Ohlc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class StreamTradeCaseWriter(val path: Path, val factors: Iterable<String>) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val stmt : String
    val tman = DataSourceTransactionManager(ds)

    init {
        val header = getHeader(tradeCaseColDefs) + factors.associateBy({ it }, { "DOUBLE PRECISION" })
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader("trades", header))
        this.stmt = makeSqlStatementFromHeader("trades", header)
    }

    fun insertTrades(trades: List<Trade>) {
        GlobalLock.lock.withLock {
            TransactionTemplate(tman).execute({ status ->
                val cases = trades.toTradingCases().map { toMapForSqlUpdate(it, tradeCaseColDefs) + it.first.tradeStat.factors }.toTypedArray()
                NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
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

    fun insertOrders(orders: List<Order>) {
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