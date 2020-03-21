package firelib.core.report

import firelib.common.Trade
import firelib.core.misc.pnl
import firelib.core.misc.SqlUtils.makeCreateSqlStmtFromHeader
import firelib.core.misc.SqlUtils.makeSqlStatementFromHeader
import firelib.core.domain.Side
import firelib.core.misc.SqlUtils
import firelib.core.report.dao.ColDef
import firelib.model.DummyModel
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path


val tradeCaseColDefs: Array<ColDef<Pair<Trade, Trade>, out Any>> = arrayOf(
    makeMetric("Ticker", { it.first.security() }),
    makeMetric("OrderId0", { it.first.order.id }),
    makeMetric("OrderId1") { it.second.order.id },

    makeMetric("epochTimeMs") { it.first.dtGmt.toEpochMilli() },

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

val tradeColDefs: Array<ColDef<Trade, out Any>> = arrayOf(
    makeMetric("Ticker", { it.security() }),
    makeMetric("OrderId0", { it.order.id }),
    makeMetric("epochTimeMs") { it.dtGmt.toEpochMilli() },
    makeMetric("EntryPriceTime") { it.priceTime },
    makeMetric("BuySell") { if (it.side() == Side.Buy) 1 else -1 },
    makeMetric("EntryDate") { it.dtGmt },
    makeMetric("EntryPrice") { it.price },
    makeMetric("Qty") { it.qty },
    makeMetric("PosAfter") { it.positionAfter }
)


class StreamTradeCaseWriter(val path: Path, val tableName: String) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    var stmt: String? = null
    val tman = DataSourceTransactionManager(ds)


    fun initTableIfNeeded(trade: Trade) {
        val header = getHeader(tradeCaseColDefs) + trade.tradeStat.factors.mapValues { "DOUBLE PRECISION" }
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(tableName, header))
        this.stmt = makeSqlStatementFromHeader(tableName, header)
    }

    fun insertTrades(trades: List<Pair<Trade, Trade>>) {
        if (trades.isEmpty()) return
        initTableIfNeeded(trades[0].first)
        TransactionTemplate(tman).execute { status ->
            val cases =
                trades.map { toMapForSqlUpdate(it, tradeCaseColDefs) + it.first.tradeStat.factors }.toTypedArray()
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
        }
    }
}

class StreamTradeWriter(val path: Path, val tableName: String) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    var stmt: String? = null
    val tman = DataSourceTransactionManager(ds)


    fun initTableIfNeeded(trade: Trade) {
        val header = getHeader(tradeColDefs) + trade.tradeStat.factors.mapValues { "DOUBLE PRECISION" }
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(tableName, header))
        this.stmt = makeSqlStatementFromHeader(tableName, header)
    }

    fun insertTrades(trades: List<Trade>) {
        if (trades.isEmpty()) return
        initTableIfNeeded(trades[0])
        TransactionTemplate(tman).execute { status ->
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, trades.map { toMapForSqlUpdate(it, tradeColDefs) + it.tradeStat.factors }.toTypedArray())
        }
    }
}
