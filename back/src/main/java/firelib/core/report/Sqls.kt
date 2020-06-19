package firelib.core.report

import firelib.core.misc.SqlUtils
import org.springframework.jdbc.core.JdbcTemplate
import java.nio.file.Path
import java.nio.file.Paths
import javax.sql.DataSource


data class OmPosition(
    val position : Int,
    val posTime : Long
)

data class ModelNameTicker(val modelName : String, val ticker : String)

object Sqls {
    val curPosSql = """
select st.ModelName, st.Ticker, st.PosAfter, st.epochTimeMs
from trades st,
     (select ModelName, Ticker, max(t.epochTimeMs) as epochTimeMs
      from trades t
      group by t.Ticker, t.ModelName) le
where st.epochTimeMs = le.epochTimeMs
  and st.Ticker = le.Ticker and st.ModelName = le.ModelName and st.PosAfter <> 0
  """.trimIndent()

    fun readCurrentPositions(path: Path): Map<ModelNameTicker, OmPosition> {
        val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
        if(!checkTableExists(ds, "trades")){
            println("empty trades for path ${path} no positions to be restored")
            return emptyMap()
        }
        return JdbcTemplate(ds).query(curPosSql) { rs, idx ->
            Pair(ModelNameTicker(rs.getString("ModelName"), rs.getString("ticker")), OmPosition(rs.getInt("posafter"), rs.getLong("epochTimeMs")))
        }.toMap()
    }

    fun checkTableExists(ds : DataSource, tableName : String) : Boolean{
        return JdbcTemplate(ds).queryForObject("SELECT count(*) FROM sqlite_master WHERE type='table' AND lower(name)='${tableName.toLowerCase()}'", Int::class.java) > 0
    }

    fun listAllTables(ds : DataSource) : List<String>{
        return JdbcTemplate(ds).queryForList("SELECT name FROM sqlite_master WHERE type='table' ", String::class.java)
    }
}


fun main() {
    println(Sqls.readCurrentPositions(Paths.get("/home/ivan/tmp/VolatilityBreak.db")))
}