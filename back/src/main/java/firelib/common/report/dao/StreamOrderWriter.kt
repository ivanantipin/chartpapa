package firelib.common.report.dao

import firelib.common.Order
import firelib.common.report.SqlUtils
import firelib.common.report.getHeader
import firelib.common.report.orderColsDefs
import firelib.common.report.toMapForSqlUpdate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path

class StreamOrderWriter(val path: Path) {
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val tman = DataSourceTransactionManager(ds)
    private val stmt: String

    init {
        val header = getHeader(orderColsDefs)
        JdbcTemplate(ds).execute(SqlUtils.makeCreateSqlStmtFromHeader("orders", header))
        stmt = SqlUtils.makeSqlStatementFromHeader("orders", header)
    }

    fun insertOrders(orders: List<Order>) {
        TransactionTemplate(tman).execute{
            val cases = orders.map { toMapForSqlUpdate(it, orderColsDefs) }.toTypedArray()
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
        }
    }
}