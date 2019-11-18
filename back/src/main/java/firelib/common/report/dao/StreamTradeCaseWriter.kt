package firelib.common.report.dao

import firelib.common.Trade
import firelib.common.misc.pnl
import firelib.common.report.*
import firelib.common.report.SqlUtils.makeCreateSqlStmtFromHeader
import firelib.common.report.SqlUtils.makeSqlStatementFromHeader
import firelib.domain.Side
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path


val tradeCaseColDefs: Array<ColDef<Pair<Trade, Trade>, out Any>> = arrayOf(
        makeMetric("Ticker", { it.first.security() }),
        makeMetric("OrderId0", { it.first.order.id }),
        makeMetric("OrderId1") { it.second.order.id },
        makeMetric("EntryPriceTime") { it.first.priceTime },
        makeMetric("ExitPriceTime") { it.second.priceTime },
        makeMetric("BuySell") { if (it.first.side() == Side.Buy) 1 else -1 },

        makeMetric("EntryDate") { it.first.dtGmt },
        makeMetric("EntryPrice") { it.first.price },
        makeMetric("ExitDate") { it.second.dtGmt },
        makeMetric("ExitPrice") { it.second.price },
        makeMetric("Pnl") { it.pnl() },
        makeMetric("Qty") { it.first.qty },
        makeMetric("MAE") { it.second.tradeStat.MAE() },
        makeMetric("MFE") { it.second.tradeStat.MFE() }
)


class StreamTradeCaseWriter(val path: Path) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    var stmt: String? = null
    val tman = DataSourceTransactionManager(ds)


    fun initTableIfNeeded(trade: Trade) {
        val header = getHeader(tradeCaseColDefs) + trade.tradeStat.factors.mapValues { "DOUBLE PRECISION" }
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader("trades", header))
        this.stmt = makeSqlStatementFromHeader("trades", header)
    }

    fun insertTrades(trades: List<Pair<Trade, Trade>>) {
        if (trades.isEmpty()) return
        initTableIfNeeded(trades[0].first)
        TransactionTemplate(tman).execute({ status ->
            val cases = trades.map { toMapForSqlUpdate(it, tradeCaseColDefs) + it.first.tradeStat.factors }.toTypedArray()
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
        })
    }
}