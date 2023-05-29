package com.firelib.reportrest

import firelib.common.Trades
import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.domain.Side
import firelib.core.domain.TradeStat
import firelib.core.misc.JsonHelper
import firelib.core.misc.atMoscow
import firelib.core.store.MdStorageImpl
import firelib.stockviz.api.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/api/v1")
open class ReportController {

    @GetMapping(
        value = ["/candles/{timeframe}/{symbol}/"],
        produces = ["application/json"]
    )
    fun candlesRead(symbol: String, timeframe: String, fromTs: Long, toTs: Long
    ): List<Candle> {

        val storage = MdStorageImpl()
        val dao = storage.daos.getDao(SourceName.MOEX, Interval.Min10)
        val ohlcs = dao.queryAll(symbol.replace(".MICEX", ""), Instant.ofEpochMilli(fromTs).atMoscow())

        return ohlcs.map {
            Candle(
                it.endTime.toEpochMilli(),
                it.open.toBigDecimal(),
                it.high.toBigDecimal(),
                it.low.toBigDecimal(),
                it.close.toBigDecimal(),
                it.volume.toInt()
            )
        }.toList()
    }


    @GetMapping(value = ["/portfolios/{portfolio}/available-instruments-meta/"], produces = ["application/json"])
    fun portfoliosAvailableInstrumentsMetaList(portfolio: String): PortfolioInstrumentsMeta {
        return PortfolioInstrumentsMeta(emptyList(), emptyList())
    }

    @GetMapping(value = ["/portfolios/{portfolio}/available-tags/"], produces = ["application/json"])
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
                        { DiscreteMeta(facName, emptyList()) })


                    if(!dmeta.values.contains(lbl)){
                        descMeta[facName] = dmeta.copy(values = dmeta.values + lbl)
                    }

                }
            }
            TagsMetaSummary(continuousMeta.values.toList(), descMeta.values.toList())
        }
    }

    @GetMapping("/portfolios/")
    fun portfoliosList(): List<Portfolio> {
        return transaction {  Trades.slice(Trades.ModelName).selectAll().withDistinct(true).map {
            Portfolio(it[Trades.ModelName], System.currentTimeMillis())
        }}
    }

    @GetMapping(value = ["/portfolios/{portfolio}/orders/"], produces = ["application/json"])
    fun portfoliosOrdersList(portfolio: String?): List<Order> {
        return emptyList()
    }

    @GetMapping(value = ["/portfolios/describe/{tradeId}"], produces = ["application/json"])
    fun displayTrade(tradeId: String): String {
        return transaction {
            val row = Trades.select { Trades.tradeId eq tradeId }.firstOrNull()
            String(row!![Trades.context]!!.bytes)
        }
    }


    @GetMapping("/portfolios/{portfolio}/trades/")
    fun portfoliosTradesList(
        @PathVariable portfolio: String
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
