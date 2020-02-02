package firelib.common.core.report.dao

import firelib.common.core.report.ColDef
import firelib.common.core.report.SqlUtils
import firelib.common.core.report.getHeader
import firelib.common.core.report.toMapForSqlUpdate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path

class ColDefDao<T>(val path: Path, val colDefs : Array<ColDef<T, out Any>>, val tableName : String) {
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val tman = DataSourceTransactionManager(ds)
    private val stmt = SqlUtils.makeSqlStatementFromHeader(tableName, getHeader(colDefs))
    init {
        JdbcTemplate(ds).execute(SqlUtils.makeCreateSqlStmtFromHeader(tableName, getHeader(colDefs)))
    }
    fun upsert(entities: List<T>) {
        TransactionTemplate(tman).execute{
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, entities.map { toMapForSqlUpdate(it, colDefs) }.toTypedArray())
        }
    }
}