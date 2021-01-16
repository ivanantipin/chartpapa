package firelib.stockviz.api

import com.fasterxml.jackson.databind.SerializationFeature
import firelib.common.Trades
import firelib.core.domain.Side
import firelib.core.domain.TradeStat
import firelib.core.misc.JsonHelper
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

@Controller("/api/v1")
class PortfoliosApiController {

    @Get(value = "/portfolios/{portfolio}/available-instruments-meta/", produces = ["application/json"])
    fun portfoliosAvailableInstrumentsMetaList(portfolio: String): PortfolioInstrumentsMeta {
        return PortfolioInstrumentsMeta(emptyList(), emptyList())
    }

    @Get(value = "/portfolios/{portfolio}/available-tags/", produces = ["application/json"])
    fun portfoliosAvailableTagsList(portfolio: String): TagsMetaSummary {
        return transaction {
            val continuousMeta = mutableMapOf<String, ContinuousMeta>()
            val descMeta = mutableMapOf<String, DiscreteMeta>()

            mapTrades(portfolio).forEach { trd->
                trd.continuousTags.forEach { facName, fac ->
                    val meta = continuousMeta.computeIfAbsent(
                        facName,
                        { ContinuousMeta(facName, BigDecimal(Int.MAX_VALUE), BigDecimal(Int.MIN_VALUE)) })

                    continuousMeta[facName] = meta.copy(max=meta.max.max(fac), min = meta.min.min(fac))
                }

                trd.discreteTags.forEach { facName, lbl ->
                    val dmeta = descMeta.computeIfAbsent(
                        facName,
                        { DiscreteMeta(facName, emptyList())})


                    if(!dmeta.values.contains(lbl)){
                        descMeta[facName] = dmeta.copy(values = dmeta.values + lbl)
                    }

                }
            }
            TagsMetaSummary(continuousMeta.values.toList(), descMeta.values.toList())
        }
    }

    @Get(value = "/portfolios/", produces = ["application/json"])
    fun portfoliosList(): List<Portfolio> {
        return transaction {  Trades.slice(Trades.ModelName).selectAll().withDistinct(true).map {
            Portfolio(it[Trades.ModelName], System.currentTimeMillis())
        }}
    }

    @Get(value = "/portfolios/{portfolio}/orders/", produces = ["application/json"])
    fun portfoliosOrdersList(portfolio: String): List<Order> {
        return emptyList()
    }

    @Get(value = "/portfolios/{portfolio}/trades/", produces = ["application/json"])
    fun portfoliosTradesList(
        portfolio: String
    ): List<Trade> {

        return transaction {
            mapTrades(portfolio)
        }
    }

    private fun mapTrades(portfolio: String) = Trades.select { Trades.ModelName eq portfolio }.map {

        var stat = TradeStat()
        val bbb = it.getOrNull(Trades.factors)
        if (bbb != null) {
            stat = JsonHelper.fromJson(String(bbb.bytes))
        }

        Trade(
            tradeId = it[Trades.tradeId],
            portfolio = it[Trades.ModelName],
            side = if (it[Trades.buySell] == 1) Side.Buy else Side.Sell,
            qty = it[Trades.qty].toBigDecimal(),
            openTime = it[Trades.epochTimeMs],
            closeTime = it[Trades.closeTimeMs],
            openPrice = it[Trades.entryPrice].toBigDecimal(),
            closePrice = it[Trades.exitPrice].toBigDecimal(),
            pnl = it[Trades.pnl].toBigDecimal(),
            symbol = it[Trades.ticker],
            continuousTags = stat.factors.associateBy({ it.first }, { it.second.toBigDecimal() }),
            discreteTags = stat.discreteFactors.associateBy({ it.first }, { it.second.toString() }),
        )
    }
}