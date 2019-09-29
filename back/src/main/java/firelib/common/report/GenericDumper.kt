package firelib.common.report

import com.funstat.store.SqlUtils
import firelib.common.report.SqlUtils.makeCreateSqlStmtFromHeader
import firelib.common.report.SqlUtils.makeSqlStatementFromHeader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class GenericDumper<T : Any>(val name : String, val path : Path, val type : KClass<T>){
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val tman = DataSourceTransactionManager(ds)

    val stmt : String

    init {
        val header = type.memberProperties.associateBy({ it.name }, { SqlTypeMapper.mapType(it.returnType) })
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(name, header))
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