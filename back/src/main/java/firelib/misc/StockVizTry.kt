package firelib.misc

import firelib.common.Order
import firelib.common.Trade
import firelib.core.domain.Side
import firelib.core.misc.pnl
import org.openapitools.client.apis.InstrumentsApi
import org.openapitools.client.apis.PortfoliosApi
import org.openapitools.client.models.NewInstrument
import org.openapitools.client.models.NewOrder
import org.openapitools.client.models.NewTrade
import org.openapitools.client.models.Portfolio
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*


fun PortfoliosApi.createIfMissing(portfolio: String){
    if(!this.portfoliosList().any { it.name == portfolio }){
        this.portfoliosCreateCreate(Portfolio(portfolio, createdDate = OffsetDateTime.now()))
    }
}

object StockVizTradeWriter {


    fun writePairs(
        list: List<Pair<Trade, Trade>>,
        orders: List<Order>,
        portfolio: String
    ) {
        val api = PortfoliosApi()

        api.createIfMissing(portfolio)

        api.portfoliosClearCreate(portfolio)

        val instrumentsApi = InstrumentsApi()

        val instrs = instrumentsApi.instrumentsList().groupBy { it.symbolAndExchange }

        val distinct = list.map {
            val instr = it.first.order.instr
            NewInstrument("${instr.code.toUpperCase()}", "MICEX", emptyMap(), emptyMap())
        }.distinct().filter {
            !instrs.containsKey(it.symbol + "." + it.exchange)
        }

        if (distinct.isNotEmpty()) {
            instrumentsApi.instrumentsAddCreate(distinct.toTypedArray())
        }
        
//        api.portfoliosAddOrdersCreate(portfolio, orders.map {
//            NewOrder(
//                orderId = it.id,
//                side = if(it.side ==Side.Sell) NewOrder.Side.sell else NewOrder.Side.buy,
//                orderType = NewOrder.OrderType.market,
//                status = NewOrder.Status.filled,
//                qty = it.qtyLots.toBigDecimal(),
//                placeTime = it.placementTime.toEpochMilli(),
//                updateTime = it.placementTime.toEpochMilli(),
//                symbol = it.security.toUpperCase() + "." + "MICEX",
//                id = it.id.hashCode(),
//                discreteTags = emptyMap(),
//                continuousTags = emptyMap(),
//                tradeId = it.tradeSubscription.msgs.firstOrNull()?.tradeNo,
//                price = it.price.toBigDecimal(),
//                executionPrice = it.price.toBigDecimal()
//            )
//        }.filter { instrs.containsKey(it.symbol) }.toTypedArray())

        api.portfoliosAddTradesCreate(portfolio, list.map {
            val t0 = it.first
            val t1 = it.second

            val descrete: Map<String, Double> = t0.tradeStat.factors.filter { it.key.endsWith("_int") }

            NewTrade(
                tradeId = t0.tradeNo,
                side = if (t0.side() == Side.Sell) NewTrade.Side.short else NewTrade.Side.long,
                qty = t0.qty.toBigDecimal(),
                openPrice = t0.price.toBigDecimal(),
                openTime = t0.dtGmt.toEpochMilli(),
                symbol = "${t0.security().toUpperCase()}.MICEX",
                pnl = it.pnl().toBigDecimal(),
                closePrice = t1.price.toBigDecimal(),
                closeTime = t1.dtGmt.toEpochMilli(),
                continuousTags = t0.tradeStat.factors.mapValues { it.value.toBigDecimal() },
                discreteTags = emptyMap<String, String>()
            )
        }.toTypedArray())
    }
}

fun mkTrd(portfolio: String): NewTrade {
    return NewTrade(
        tradeId = UUID.randomUUID().toString(),
        side = NewTrade.Side.long,
        qty = 1.toBigDecimal(),
        openPrice = 1.toBigDecimal(),
        openTime = Instant.now().toEpochMilli(),
        symbol = "SBER.MICEX",
        pnl = 2.toBigDecimal(),
        closePrice = 1.toBigDecimal(),
        closeTime = Instant.now().toEpochMilli(),
        continuousTags = mapOf<String, BigDecimal>(
            "Factor0" to 1.0.toBigDecimal()
        ),
        discreteTags = mapOf<String, String>(
            "Factor0" to "A0"
        )
    )
}

fun main() {
    val api = PortfoliosApi()
    val portfolio = "Dummy0"

    //api.portfoliosDeleteDelete(portfolio)

    //api.portfoliosCreateCreate(Portfolio(name=portfolio))

    println(api.portfoliosOrdersList("VolatilityBreak").toList())



}


