package firelib.core.report

import firelib.common.Trade
import firelib.core.domain.Side
import firelib.core.misc.SqlUtils
import firelib.core.misc.SqlUtils.makeCreateSqlStmtFromHeader
import firelib.core.misc.SqlUtils.makeSqlStatementFromHeader
import firelib.core.misc.pnl
import firelib.core.report.dao.ColDef
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap


val tradeCaseColDefs: Array<ColDef<Pair<Trade, Trade>, out Any>> = arrayOf(
    makeMetric("Ticker", { it.first.security() }),
    makeMetric("TradeId", { it.first.tradeNo }),
    makeMetric("ModelName", { it.first.order.modelName }),
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


class StreamTradeCaseWriter(val path: Path, val tableName: String) {

    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    var stmt: String? = null
    val tman = DataSourceTransactionManager(ds)

    val factorsTables = ConcurrentHashMap<String,String>()


    fun initTableIfNeeded(trade: Trade) {
        val header = getHeader(tradeCaseColDefs)
        JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(tableName, header))
        this.stmt = makeSqlStatementFromHeader(tableName, header)
    }

    fun initFactorsTableIfNeeded(trade: Trade) : String{
        return factorsTables.computeIfAbsent(trade.order.modelName, {
            if(trade.tradeStat.factors.isEmpty()){
                "NA"
            }else{
                val header =  trade.tradeStat.factors.associateBy({it.first}, {"DOUBLE PRECISION"})  + Pair("TradeId","VARCHAR")
                JdbcTemplate(ds).execute(makeCreateSqlStmtFromHeader(getFactorsTable(trade), header))
                makeSqlStatementFromHeader(getFactorsTable(trade), header)
            }
        })
    }

    private fun getFactorsTable(trade: Trade) = "${trade.order.modelName}_factors"


    fun insertTrades(trades: List<Pair<Trade, Trade>>) {
        if (trades.isEmpty()) return
        initTableIfNeeded(trades[0].first)
        TransactionTemplate(tman).execute { status ->
            val cases =
                trades.map { toMapForSqlUpdate(it, tradeCaseColDefs) }.toTypedArray()
            NamedParameterJdbcTemplate(ds).batchUpdate(stmt, cases)
        }
        insertFactors(trades)
    }

    fun insertFactors(trades: List<Pair<Trade, Trade>>) {
        val groupBy = trades.groupBy { it.first.order.modelName }

        groupBy.forEach({model,trades->

            val factorStmt = initFactorsTableIfNeeded(trades[0].first)

            if(factorStmt != "NA"){
                TransactionTemplate(tman).execute { status ->
                    val cases =
                        trades.map { it.first.tradeStat.factors.associate { it } + Pair("TradeId", it.first.tradeNo) }.toTypedArray()
                    NamedParameterJdbcTemplate(ds).batchUpdate(factorStmt, cases)
                }
            }
        })

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

