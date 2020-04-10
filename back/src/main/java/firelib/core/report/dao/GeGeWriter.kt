package firelib.core.report.dao

import firelib.core.misc.SqlUtils
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class GeGeWriter<T : Any>(
    val path: Path,
    val type: KClass<T>,
    val pk: List<String> = emptyList(),
    val name: String = type.simpleName!!
) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val tman = DataSourceTransactionManager(ds)

    val stmt: String

    val sel: String

    val mappers : Map<String,(Any)->(Any)>
    val mappersTo: Map<String, (Any) -> Any>

    val members = type.memberProperties

    init {
        val header = members.associateBy({ it.name }, { SqlTypeMapper.mapType(it.returnType) })

        mappers = members.associateBy({ it.name }, {
            SqlTypeMapper.fromDb(it.returnType)
        })

        mappersTo = members.associateBy({ it.name }, {
            SqlTypeMapper.toDb(it.returnType)
        })

        JdbcTemplate(ds).execute(
            SqlUtils.makeCreateSqlStmtFromHeader(
                name,
                header,
                pk
            )
        )
        stmt = SqlUtils.makeSqlStatementFromHeader(name, header)
        sel = "select * from $name"
    }

    fun write(rows: List<T>) {
        val rowsM = rows.map { row ->
            members.associateBy({ it.name }, { mappersTo[it.name]!!(it.get(row)!!)  })
        }
        TransactionTemplate(tman).execute { status ->
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, rowsM.toTypedArray())
        }
    }

    fun read(): List<T> {
        return NamedParameterJdbcTemplate(ds).query(sel) { rs, row ->
            val cons = type.primaryConstructor!!
            val params = cons.parameters
            cons.callBy(params.associateBy({ it }, {
                mappers[it.name]!!(rs.getObject(it.name))
            }))
        }
    }
}
