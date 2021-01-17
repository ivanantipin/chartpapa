package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.core.misc.toStrWithDecPlaces
import kotlin.math.log

val defaultLegend = HLegend(
    floating = false,
    borderWidth = 1,
    borderColor = "#444444",
    enabled = true
)


object ChartCreator {
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
            chart = HChart(margin = listOf(0, 20, 50, 50)),
            rangeSelector = HRangeSelector(false),
            legend = defaultLegend
        ).apply {
            yAxis += HAxis(height = "100%", lineWidth = 1, offset = 10, opposite = false)
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1)
            series += HSeries("ohlc", "price", data = data, marker = HMarker(true), showInLegend = true)
            navigator = HNavigator(false)
            scrollbar = HScrollbar(false)
        }
    }

    fun markLevel(
        timeMs : Long,
        level : Double,
        below : Boolean
    ): HLabel {
        val decPlaces =  log(100000 / level, 10.0).toInt()
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

    fun makeSequentaOpts(
        ann: SequentaAnnnotations,
        hours: List<Ohlc>,
        title: String
    ): HOptions {
        val series = HiChartCreator.renderHLines(ann.lines)

        val options = makeOptions(hours, title)

        options.annotations = listOf(HAnnotation(labels = ann.labels, shapes = ann.shapes))

        options.series += series
        return options
    }

}