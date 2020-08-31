package firelib.core.report

import firelib.core.domain.ModelNameTicker
import firelib.core.domain.OmPosition
import firelib.core.misc.SqlUtils
import org.springframework.jdbc.core.JdbcTemplate
import java.nio.file.Path

object SqlQueries {
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
        if (!SqlUtils.checkTableExists(ds, "trades")) {
            println("empty trades for path ${path} no positions to be restored")
            return emptyMap()
        }
        return JdbcTemplate(ds).query(curPosSql) { rs, idx ->
            Pair(
                ModelNameTicker(rs.getString("ModelName"), rs.getString("ticker")),
                OmPosition(rs.getInt("posafter"), rs.getLong("epochTimeMs"))
            )
        }.toMap()
    }

    fun readProps(path: Path, table: String, env: String): Map<String, String> {
        val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
        if (!SqlUtils.checkTableExists(ds, table)) {
            println("empty table for path ${path} ")
            return emptyMap()
        }
        return JdbcTemplate(ds).query("select name, value from ${table} where env = '${env}'") { rs, idx ->
            rs.getString("name") to rs.getString("value")
        }.toMap()
    }


}