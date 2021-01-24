package firelib.common

import firelib.core.domain.TradeStat
import firelib.core.misc.dbl2Str
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths
import java.time.Instant
import kotlin.math.absoluteValue


data class Trade(
    val qty: Int,
    val price: Double,
    val order: Order,
    val dtGmt: Instant,
    val priceTime: Instant,
    val tradeStat: TradeStat = TradeStat(),
    val positionAfter: Int = 0,
    val tradeNo : String = order.id,
    var tradeCtxValue : Any = "",
    var contextSupplier : ((openTrade : Trade, closingTrade : Trade)->ByteArray) = {t0,t1-> ByteArray(0) }
) {

    init {
        require(qty >= 0, { "amount can't be negative" })
        require(order != null, { "order must be present" })
        require(!price.isNaN(), { "price must be valid" })

        if(price.absoluteValue < 0.000001){
            println()
        }
    }

    fun security() = order.security

    fun side() = order.side

    fun adjustPositionByThisTrade(position: Int): Int = position + order.side.sign * qty

    fun moneyFlow(): Double {
        return -qty * price * order.side.sign
    }

    fun pnl(aPrice: Double): Double {
        return moneyFlow() - qty * aPrice * order.side.opposite().sign
    }

    fun split(amt: Int): Pair<Trade, Trade> {
        return Pair(copy(qty = amt), copy(qty = (qty - amt)))
    }

    override fun toString(): String {
        return "Trade(price=${dbl2Str(price,2)} qty=$qty side=${side()} dtGmt=$dtGmt orderId=${order.id} sec=${security()} posAfter=${positionAfter})"
    }

}

object Trades : Table("trades") {
    val ticker = varchar("Ticker", 10)
    val tradeId = varchar("TradeId", 20)
    val ModelName = varchar("ModelName", 20)
    val orderId0 = varchar("OrderId0", 20)
    val orderId1 = varchar("OrderId1", 20)
    val epochTimeMs = long("epochTimeMs")
    val closeTimeMs = long("closeTimeMs")
    val entryPriceTime = long("EntryPriceTime")
    val exitPriceTime = long("ExitPriceTime")
    val buySell = integer("BuySell")
    val entryTime = long("EntryDate")
    val entryPrice = double("EntryPrice")
    val exitTime = long("ExitDate")
    val exitPrice = double("ExitPrice")
    val pnl = double("Pnl")
    val qty = integer("Qty")
    val factors = blob("Factors").nullable()
    val context = blob("Context").nullable()
}



fun initDatabase() {
    val path = Paths.get("/home/ivan/projects/chartpapa/market_research/report_out/report.db")

    Database.connect(
        "jdbc:sqlite:${path.toAbsolutePath()}?journal_mode=WAL",
        driver = "org.sqlite.JDBC"
    )
}

fun main() {

    initDatabase()

    transaction {

        SchemaUtils.createMissingTablesAndColumns(Trades)

        Trades.selectAll().forEach {
            println(it)
        }
    }


}


