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

class TradeStat(val stratName : String, val description : String, val strats : Broadcaster<StratDescription>){


    val generators = ConcurrentHashMap<String, StreamTradeCaseGenerator>();
    val closedPoses = ConcurrentHashMap<String, Queue<Pair<Trade, Trade>>>();
    val defferer = Defferer()

    var stratBuilder = StratDescription.newBuilder().apply {
        name = stratName
        description = description
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

            println("publish strat to observers")

            strats.add(stratBuilder.build())

        }
    }


}