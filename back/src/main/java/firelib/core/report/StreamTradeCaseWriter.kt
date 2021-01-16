package firelib.core.report

import firelib.common.Trade
import firelib.common.Trades
import firelib.common.Trades.nullable
import firelib.core.domain.Side
import firelib.core.misc.JsonHelper
import firelib.core.misc.SqlUtils
import firelib.core.misc.SqlUtils.makeCreateSqlStmtFromHeader
import firelib.core.misc.SqlUtils.makeSqlStatementFromHeader
import firelib.core.misc.pnl
import firelib.core.report.dao.ColDef
import kotlinx.coroutines.channels.ticker
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

val tradeColDefs: Array<ColDef<Trade, out Any>> = arrayOf(
    makeMetric("Ticker", { it.security() }),
    makeMetric("TradeId", { it.tradeNo }),
    makeMetric("ModelName", { it.order.modelName }),
    makeMetric("OrderId0", { it.order.id }),
    makeMetric("epochTimeMs") { it.dtGmt.toEpochMilli() },
    makeMetric("EntryPriceTime") { it.priceTime },
    makeMetric("BuySell") { if (it.side() == Side.Buy) 1 else -1 },
    makeMetric("EntryDate") { it.dtGmt },
    makeMetric("EntryPrice") { it.price },
    makeMetric("Qty") { it.qty },
    makeMetric("PosAfter") { it.positionAfter }
)

fun initDatabase() {
    val path = Paths.get("/home/ivan/projects/chartpapa/market_research/report_out/report.db")

    Database.connect(
        "jdbc:sqlite:${path.toAbsolutePath()}?journal_mode=WAL",
        driver = "org.sqlite.JDBC"
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Trades)
        SchemaUtils.createMissingTablesAndColumns(Opts)
    }
}


class StreamTradeCaseWriter(val path: Path, val tableName: String) {

    init {
        initDatabase()
    }


    fun insertTrades(trades: List<Pair<Trade, Trade>>) {
        if (trades.isEmpty()) return

        transaction {
            Trades.batchInsert(trades){
                this[Trades.ticker] =  it.first.security()
                this[Trades.tradeId] =  it.first.tradeNo
                this[Trades.ModelName] = it.first.order.modelName
                this[Trades.orderId0] =  it.first.order.id
                this[Trades.orderId1] = it.second.order.id
                this[Trades.epochTimeMs] = it.first.dtGmt.toEpochMilli()
                this[Trades.closeTimeMs] = it.second.dtGmt.toEpochMilli()
                this[Trades.entryPriceTime] =  it.first.priceTime.toEpochMilli()
                this[Trades.exitPriceTime] = it.second.priceTime.toEpochMilli()
                this[Trades.buySell] = if (it.first.side() == Side.Buy) 1 else -1
                this[Trades.entryTime] = it.first.dtGmt.toEpochMilli()
                this[Trades.entryPrice] = it.first.price
                this[Trades.exitPrice] = it.second.price
                this[Trades.exitTime] = it.second.dtGmt.toEpochMilli()
                this[Trades.pnl] = it.pnl()
                this[Trades.qty] = it.first.qty
                this[Trades.factors] = ExposedBlob(JsonHelper.toJsonBytes(it.first.tradeStat))
            }
        }
    }

}

class StreamTradeWriter(val path: Path, val tableName: String) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    var stmt: String? = null
    val tman = DataSourceTransactionManager(ds)


    fun initTableIfNeeded(trade: Trade) {
        val header = getHeader(tradeColDefs)
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(tableName, header))
        this.stmt = makeSqlStatementFromHeader(tableName, header)
    }

    fun insertTrades(trades: List<Trade>) {
        if (trades.isEmpty()) return
        initTableIfNeeded(trades[0])
        TransactionTemplate(tman).execute { status ->
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, trades.map { toMapForSqlUpdate(it, tradeColDefs) }.toTypedArray())
        }
    }
}

