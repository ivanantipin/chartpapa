package com.firelib.techbot.chart

import com.firelib.techbot.TdLine
import com.firelib.techbot.chart.domain.HAnnotation
import com.firelib.techbot.chart.domain.HMarker
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.chart.domain.HSeries
import com.firelib.techbot.domain.LineType
import firelib.core.domain.Ohlc
import firelib.core.domain.Side

object TrendLinesRenderer {

    fun renderLine(
        line: TdLine,
        idx: Int,
        hours: List<Ohlc>
    ): Sequence<HSeries> {
        val color = if (line.lineType == LineType.Support) "green" else "red"

        var name = "$idx"
        var showInLegend = false

        if (idx == 0) {
            name = if (line.lineType == LineType.Resistance) "resistance" else "support"
            showInLegend = true
        }

        return sequence {
            val data: List<Array<Double>> = listOf(
                arrayOf(hours[line.x0].endTime.toEpochMilli().toDouble(), line.y0),
                arrayOf(hours[line.x1].endTime.toEpochMilli().toDouble(), line.y1)
            )
            yield(
                HSeries(
                    "line",
                    name = "name",
                    HMarker(false),
                    data,
                    showInLegend = false,
                    color = color,
                    dashStyle = "dash",
                    lineWidth = 1.0
                )
            )

            val data2nd: List<Array<Double>> = if (line.intersectPoint != null) {
                listOf(
                    arrayOf(hours[line.x1].endTime.toEpochMilli().toDouble(), line.y1),
                    arrayOf(
                        hours[line.intersectPoint!!.first].endTime.toEpochMilli().toDouble(),
                        line.intersectPoint!!.second
                    )
                )

            } else {
                listOf(
                    arrayOf(hours[line.x1].endTime.toEpochMilli().toDouble(), line.y1),
                    arrayOf(
                        hours.last().endTime.toEpochMilli().toDouble(),
                        line.calcValue(hours.size - 1)
                    )
                )
            }
            yield(
                HSeries(
                    "line",
                    name = name,
                    HMarker(true),
                    data2nd,
                    showInLegend = showInLegend,
                    color = color,
                    dashStyle = "dot"
                )
            )
        }
    }

    fun trendLinesWithoutBySell(
        hours: List<Ohlc>,
        title: String,
        lines: List<TdLine>
    ): HOptions {
        val options = RenderUtils.makeOptions(hours, title)
        val series = lines.groupBy { it.lineType }.mapValues { (_, value) ->
            value.asSequence().flatMapIndexed { idx, line ->
                renderLine(line, idx, hours)
            }
        }.values.flatMap { it.toList() }
        options.series += series
        return options
    }

    fun makeTrendLines(
        hours: List<Ohlc>,
        title: String,
        lines: List<TdLine>
    ): HOptions {
        val options = trendLinesWithoutBySell(hours, title, lines)
        options.annotations += annotations(lines, hours)
        return options
    }

    fun annotations(lines: List<TdLine>, hours: List<Ohlc>): HAnnotation {
        val shapes = lines.filter { it.intersectPoint != null }.map {
            val side = if (it.lineType == LineType.Support) Side.Sell else Side.Buy
            val color = RenderUtils.getLineColor(side)
            val x = hours[it.intersectPoint!!.first].endTime.toEpochMilli()
            val y = it.intersectPoint!!.second
            RenderUtils.makeBuySellPoint(color, x, y, side)
        }
        return HAnnotation(emptyList(), shapes)
    }

}