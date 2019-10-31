package com.firelib.stratserver

import com.firelib.Chart
import com.firelib.DatePoint
import com.firelib.Position
import com.firelib.StratDescription
import firelib.common.Trade
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.common.misc.pnl
import firelib.domain.Ohlc
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class TradeStat(val stratName : String, val description : String, val strats : Brodcaster<StratDescription>){


    val generators = ConcurrentHashMap<String, StreamTradeCaseGenerator>();
    val closedPoses = ConcurrentHashMap<String, Queue<Pair<Trade, Trade>>>();
    val prices = ConcurrentHashMap<String,Ohlc>()
    val defferer = Defferer()

    var stratBuilder : StratDescription.Builder

    init {
        this.stratBuilder = StratDescription.newBuilder().apply {
            name = stratName
            description = description
        }
    }

    fun addTrade(trade : Trade){
        val gen = generators.computeIfAbsent(trade.security(),{ StreamTradeCaseGenerator() })
        val pos = closedPoses.computeIfAbsent(trade.security(), { ConcurrentLinkedQueue<Pair<Trade, Trade>>() })
        pos += gen.addTrade(trade)

        updateThings()
    }

    private fun updateThings() {
        defferer.executeLater {
            val flatten = closedPoses
                    .flatMap { it.value }
                    .sortedBy { it.first.dtGmt }

            var cumPnl = 0.0
            val dots = flatten.map {
                val ret = DatePoint.newBuilder()
                ret.setTimestamp(it.first.dtGmt.toEpochMilli())
                ret.setLabel(it.first.security())
                cumPnl += it.pnl()
                ret.setValue(cumPnl)
                ret.build();
            }
            val chart = Chart.newBuilder().setChartType(Chart.ChartType.Line)
                    .addAllPoints(dots).build()

            stratBuilder.benchmark = chart

            stratBuilder.clearClosedPositions()

            stratBuilder.addAllClosedPositions(closedPoses.values.flatMap { it.toList() }.sortedBy { it.first.dtGmt }.map {
                Position.newBuilder()
                        .setTicker(it.first.security())
                        .setTimestamp(it.first.dtGmt.toEpochMilli())
                        .setPosition(it.first.qty.toLong())
                        .setOpenPrice(it.first.price)
                        .setCloseTimestamp(it.second.dtGmt.toEpochMilli())
                        .setClosePrice(it.second.price)
                        .setPnl(it.pnl())
                        .build()
            })

            updateClosePrices(prices)

            println("publish strat to observers")

            strats.add(stratBuilder.build())

        }
    }

    fun updateClosePrices(prices : Map<String,Ohlc>){
        stratBuilder.clearOpenPositions()

        stratBuilder.addAllOpenPositions(generators.values.flatMap { it.getPosition() }.sortedBy { it.dtGmt }.map {

            val closePrice = if(prices.containsKey(it.security())) prices[it.security()]!!.close else it.price

            Position.newBuilder()
                    .setTicker(it.security())
                    .setTimestamp(it.dtGmt.toEpochMilli())
                    .setPosition(it.qty.toLong())
                    .setOpenPrice(it.price)
                    .setCloseTimestamp(it.dtGmt.toEpochMilli())
                    .setClosePrice(closePrice)
                    .setPnl(it.pnl(closePrice))
                    .build()})
    }

    fun updatePrices(priceMap: Map<String, Ohlc>) {
        prices.putAll(priceMap)

        defferer.executeLater {
            updateThings()
        }
    }
}