package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*
import firelib.core.domain.Ohlc
import firelib.core.domain.Side


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
            chart = HChart(margin = listOf(0,20,50,50)),
            rangeSelector = HRangeSelector(false),
            legend = defaultLegend
        ).apply {
            yAxis += HAxis(height = "100%", lineWidth = 1, offset = 10, opposite = false)
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1)
            series += HSeries("ohlc", title, data = data, marker = HMarker(true), showInLegend = true)
            navigator = HNavigator(false)
            scrollbar = HScrollbar(false)
        }
    }

    fun makeBuySellPoint(color : String, time : Long, y : Double, buySell : Side) : HShape{
        val point = HPoint(0, 0, time, y)
        val sign = buySell.sign
        return HShape(fill = "none", stroke = color, strokeWidth = 1,
            dashStyle = "solid",
            type = "path",
            points = listOf(
                point.copy(y = (point.y!! - sign*point.y!!/200.0)),
                point
            ),
            markerEnd = "arrow",
            height = 2
        )
    }


}