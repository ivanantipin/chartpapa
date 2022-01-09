package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*
import com.funstat.domain.HLine
import firelib.common.Trade
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.core.misc.toStrWithDecPlaces
import kotlin.math.log


object RenderUtils {

    val defaultLegend = HLegend(
        floating = false,
        borderWidth = 1,
        borderColor = "#444444",
        enabled = true
    )


    val GLOBAL_OPTIONS_FOR_BILLIONS = mapOf("lang" to mapOf("numericSymbols" to listOf("k", "M", "B", "T", "P", "E")))

    fun makeOptions(ohlc: List<Ohlc>, title: String): HOptions {
        val data = ohlc.mapIndexed { idx, it ->
            arrayOf(
                it.endTime.toEpochMilli().toDouble(),
                it.open,
                it.high,
                it.low,
                it.close
            )
        }
        return HOptions(
            title = HTitle(title),
            chart = HChart(margin = listOf(0, 50, 50, 50)),
            rangeSelector = HRangeSelector(false),
            legend = defaultLegend
        ).apply {
            yAxis += HAxis(height = "80%", lineWidth = 1, offset = 10, opposite = false)
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1)
            series += HSeries("ohlc", "price", data = data, marker = HMarker(true), showInLegend = true)
            navigator = HNavigator(false)
            scrollbar = HScrollbar(false)
        }
    }

    fun markLevel(
        timeMs: Long,
        level: Double,
        below: Boolean
    ): HLabel {
        val decPlaces = log(100000 / level, 10.0).toInt()
        return HLabel(
            verticalAlign = if (below) "bottom" else "top",
            backgroundColor = "rgba(255,255,255,0)", //if (s.reference.up) "red" else "green",
            borderColor = "rgba(255,255,255,0.5)",
            text = level.toStrWithDecPlaces(decPlaces),
            style = HStyle(fontSize = "6px"),
            distance = if (below) -10 else 0,
            point = HPoint(x = timeMs, y = level, xAxis = 0, yAxis = 0),
            allowOverlap = true
        )
    }

    fun makeBuySellPoint(color: String, time: Long, y: Double, buySell: Side): HShape {
        val point = HPoint(0, 0, time, y)
        val sign = buySell.sign
        return HShape(
            fill = "none", stroke = color, strokeWidth = 1,
            dashStyle = "solid",
            type = "path",
            points = listOf(
                point.copy(y = (point.y!! - sign * point.y!! / 200.0)),
                point
            ),
            markerEnd = "arrow",
            height = 2
        )
    }

    fun renderHLines(lines: List<HLine>): List<HSeries> {
        val series = lines.flatMap { hline ->
            sequence {
                val data: List<Array<Double>> = listOf(
                    arrayOf(hline.start.toDouble(), hline.level),
                    arrayOf(hline.end.toDouble(), hline.level)
                )
                yield(
                    HSeries(
                        "line",
                        name = "name",
                        HMarker(false),
                        data,
                        showInLegend = false,
                        color = hline.color,
                        dashStyle = hline.dashStyle,
                        lineWidth = 0.5
                    )
                )
            }
        }
        return series
    }

    fun buySells(trades: List<Trade>): HAnnotation {
        val shapes = trades.map {
            val side = it.side()
            val color = getLineColor(side)
            val x = it.dtGmt.toEpochMilli()
            val y = it.price
            makeBuySellPoint(color, x, y, side)
        }
        return HAnnotation(emptyList(), shapes)
    }

    fun getLineColor(side: Side): String {
        return if (side == Side.Buy) "green" else "red"
    }

}