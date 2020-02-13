package firelib.core.report

import firelib.core.misc.SqlUtils
import firelib.core.report.Sqls.readCurrentPositions
import firelib.model.DummyModel
import org.springframework.jdbc.core.JdbcTemplate
import java.nio.file.Path
import javax.sql.DataSource

object Sqls {
    val curPosSql = """
select st.Ticker, st.PosAfter
from singleTrades st,
     (select Ticker, max(t.EntryPriceTime) as EntryPriceTime
      from singleTrades t
      group by t.Ticker) le
where st.EntryPriceTime = le.EntryPriceTime
  and st.Ticker = le.Ticker
  """.trimIndent()

    fun readCurrentPositions(path: Path): Map<String, Int> {
        val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
        if(!checkTableExists(ds, "singleTrades")){
            return emptyMap()
        }
        return JdbcTemplate(ds).query(curPosSql, { rs, idx ->
            Pair(rs.getString("ticker"), rs.getInt("posafter"))
        }).toMap()
    }

    fun checkTableExists(ds : DataSource, tableName : String) : Boolean{
        return JdbcTemplate(ds).queryForObject("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='${tableName.toUpperCase()}'", Int::class.java) > 0
    }

}
