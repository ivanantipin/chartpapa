package firelib.common.report

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.nio.file.Path
import kotlin.reflect.full.createType

object GenericMapWriter{
    fun write(path : Path, rows: List<Map<String,Any>>, tableName : String) {

        val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
        val stmt: String

        val colsDef = rows[0].map {
            ColDef<Map<String, Any>, Any>(it.key, { v: Map<String, Any> ->
                v[it.key]!!
            }, it.value::class.createType())
        }.toTypedArray()

        val header = getHeader(colsDef as Array<ColDef<Map<String, Any>, out Any>>)
        JdbcTemplate(ds).execute(SqlUtils.makeCreateSqlStmtFromHeader(tableName, header))
        stmt = SqlUtils.makeSqlStatementFromHeader(tableName, header)
        NamedParameterJdbcTemplate(ds).batchUpdate(stmt, rows.toTypedArray())
    }
}