package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*
import firelib.core.domain.Ohlc


val defaultLegend = HLegend(
    floating = false,
    borderWidth = 1,
    borderColor = "#444444",
    enabled = true
)


object ChartCreator {
    fun makeOptions(ohlc: List<Ohlc>, ticker: String): HOptions {
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
            title = HTitle(ticker),
            rangeSelector = HRangeSelector(false),
            legend = defaultLegend
        ).apply {
            yAxis += HAxis(height = "100%", lineWidth = 1, title = null)
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1)
            series += HSeries("ohlc", ticker, data = data, marker = HMarker(true), showInLegend = true)
            navigator = HNavigator(false)
            scrollbar = HScrollbar(false)
        }
    }
}