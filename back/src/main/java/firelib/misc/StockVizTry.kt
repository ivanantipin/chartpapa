package firelib.misc

import firelib.common.Order
import firelib.common.Trade
import firelib.core.domain.Side
import firelib.core.misc.pnl
import org.openapitools.client.apis.InstrumentsApi
import org.openapitools.client.apis.PortfoliosApi
import org.openapitools.client.models.*
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*


fun PortfoliosApi.createIfMissing(portfolio: String) {
    if (!this.portfoliosList().any { it.name == portfolio }) {
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

        //addOrders(api, portfolio, orders, instrs)

        addTrades(api, portfolio, list)
    }

    private fun addTrades(
        api: PortfoliosApi,
        portfolio: String,
        list: List<Pair<Trade, Trade>>
    ) {
        api.portfoliosAddTradesCreate(portfolio, list.map {
            val t0 = it.first
            val t1 = it.second

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
                continuousTags = t0.tradeStat.factors.associateBy({ it.first }, { it.second.toBigDecimal() }),
                discreteTags = t0.tradeStat.discreteFactors.associateBy({ it.first }, { it.second.toString() })
            )
        }.toTypedArray())
    }

    private fun addOrders(
        api: PortfoliosApi,
        portfolio: String,
        orders: List<Order>,
        instrs: Map<String, List<Instrument>>
    ) {
        api.portfoliosAddOrdersCreate(portfolio, orders.map {
            NewOrder(
                orderId = it.id,
                side = if (it.side == Side.Sell) NewOrder.Side.sell else NewOrder.Side.buy,
                orderType = NewOrder.OrderType.market,
                status = NewOrder.Status.filled,
                qty = it.qtyLots.toBigDecimal(),
                placeTime = it.placementTime.toEpochMilli(),
                updateTime = it.placementTime.toEpochMilli(),
                symbol = it.security.toUpperCase() + "." + "MICEX",
                id = it.id.hashCode(),
                discreteTags = emptyMap(),
                continuousTags = emptyMap(),
                tradeId = it.tradeSubscription.msgs.firstOrNull()?.tradeNo,
                price = it.price.toBigDecimal(),
                executionPrice = it.price.toBigDecimal()
            )
        }.filter { instrs.containsKey(it.symbol) }.toTypedArray())
    }
}