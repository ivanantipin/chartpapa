package firelib.core.report

import firelib.core.misc.SqlUtils
import firelib.core.report.dao.ColDef
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path
import kotlin.reflect.full.createType

object GenericMapWriter{
    fun write(path : Path, rows: List<Map<String,Any>>, tableName : String) {

        val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
        val stmt: String

        val tman = DataSourceTransactionManager(ds)

        val commonSet = rows.fold(rows[0].keys, { p, n ->
            n.keys.intersect(p)
        })


        val colsDef = rows[0].filter {  commonSet.contains(it.key) }.map {
            ColDef<Map<String, Any>, Any>(it.key, { v: Map<String, Any> ->
                v[it.key]!!
            }, it.value::class.createType())
        }.toTypedArray()

        val header = getHeader(colsDef as Array<ColDef<Map<String, Any>, out Any>>)
        JdbcTemplate(ds).execute(SqlUtils.makeCreateSqlStmtFromHeader(tableName, header))
        stmt = SqlUtils.makeSqlStatementFromHeader(tableName, header)

        TransactionTemplate(tman).execute { status ->
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, rows.toTypedArray())
        }


    }
}