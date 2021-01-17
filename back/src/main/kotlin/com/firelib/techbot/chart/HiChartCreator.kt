package com.firelib.techbot.chart

import com.firelib.techbot.TdLine
import com.firelib.techbot.chart.domain.HAnnotation
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.chart.domain.HSeries
import com.firelib.techbot.domain.LineType
import com.funstat.domain.HLine
import firelib.core.domain.LevelSignal
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.indicators.SR

object HiChartCreator {
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
                        com.firelib.techbot.chart.domain.HMarker(false),
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

    fun makeLevelOptions(
        hours: List<Ohlc>,
        title: String,
        lines: List<SR>
    ): HOptions {
        val options = ChartCreator.makeOptions(hours, title)

        options.series += level2series(lines, hours)

        options.annotations += com.firelib.techbot.chart.domain.HAnnotation(lines.map {
            ChartCreator.markLevel(it.initial.toEpochMilli(), it.level, false)
        }, emptyList())
        return options
    }

    fun level2series(
        lines: List<SR>,
        hours: List<Ohlc>
    ): List<HSeries> {
        return renderHLines(lines.flatMap {
            val start = it.initial.toEpochMilli()
            val end = it.activeDate.toEpochMilli()
            listOf(
                HLine(start, end, it.level, "solid", "green"),
                HLine(end, hours.last().endTime.toEpochMilli(), it.level, "dash", "green")
            )
        })
    }

    fun levelBreaches(
        hours: List<Ohlc>,
        title: String,
        signals: List<LevelSignal>
    ): HOptions {
        val options = ChartCreator.makeOptions(hours, title)

        options.series += level2series(signals.map { it.level }, hours)

        val shapes = signals.map {
            val color = getLineColor(it.side)
            ChartCreator.makeBuySellPoint(color, it.time, it.level.level, it.side)
        }

        val labels = signals.map { sr ->
            ChartCreator.markLevel(sr.level.initial.toEpochMilli(), sr.level.level, false)
        }
        options.annotations += HAnnotation(labels, shapes)
        return options
    }

    fun makeTrendLines(
        hours: List<Ohlc>,
        title: String,
        lines: List<TdLine>
    ): HOptions {
        val options = ChartCreator.makeOptions(hours, title)
        options.annotations += annotations(lines, hours)
        val series = lines.groupBy { it.lineType }.mapValues { (key, value) ->
            value.asSequence().flatMapIndexed { idx, line ->
                renderLine(line, idx, hours)
            }
        }.values.flatMap { it.toList() }
        options.series += series
        options.series += renderHLines(activeLevels(lines, hours))
        return options
    }

    private fun annotations(lines: List<TdLine>, hours: List<Ohlc>): com.firelib.techbot.chart.domain.HAnnotation {
        val shapes = lines.filter { it.intersectPoint != null }.map {
            val side = if (it.lineType == LineType.Support) Side.Sell else Side.Buy
            val color = getLineColor(side)
            val x = hours[it.intersectPoint!!.first].endTime.toEpochMilli()
            val y = it.intersectPoint!!.second
            ChartCreator.makeBuySellPoint(color, x, y, side)
        }
        return com.firelib.techbot.chart.domain.HAnnotation(emptyList(), shapes)
    }

    private fun getLineColor(side: Side): String {
        return if (side == Side.Buy) "green" else "red"
    }

    private fun activeLevels(lines: List<TdLine>, hours: List<Ohlc>): List<HLine> {
        return lines.filter { it.intersectPoint == null }.map {
            val start = hours[0].endTime.toEpochMilli()
            val end = hours[20].endTime.toEpochMilli()
            val side = if (it.lineType == LineType.Support) Side.Sell else Side.Buy
            HLine(start, end, it.calcValue(hours.size - 1), "solid", getLineColor(side))
        }
    }

    private fun renderLine(
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
                    com.firelib.techbot.chart.domain.HMarker(false),
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
                    com.firelib.techbot.chart.domain.HMarker(true),
                    data2nd,
                    showInLegend = showInLegend,
                    color = color,
                    dashStyle = "dot"
                )
            )
        }
    }
}