package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*

object Fcf2DebtCharter {

    fun makeSeries(fcf2debt : Series<String>, netDebtSeries : Series<String>, title: String): HOptions {
        return HOptions(
            title = HTitle(title),
            chart = HChart(margin = listOf(100, 70, 50, 70)),
            legend = HLegend(
                floating = false,
                borderWidth = 1,
                borderColor = "#444444",
                verticalAlign = "top",
                layout = "horizontal"
            ),

        ).apply {
            yAxis += HAxis(height = "100%", lineWidth = 1, offset = 10, title = HTitle("FCF-to-Debt", style = HStyle(color = "blue")), opposite = false)
            yAxis += HAxis(height = "100%", lineWidth = 1, offset = 10, title = HTitle("Net-Debt", style = HStyle(color = "red")), opposite = true)
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1, categories = fcf2debt.data.keys.toList())
            series +=   HSeriesColumn(null ,  "FCF-to-Debt", yAxis = 0, color = "blue", data = fcf2debt.data.values.toList(), marker = HMarker(true), showInLegend = true)
            series +=   HSeriesColumn(null ,  "Net-Debt", yAxis = 1, color = "red", data = netDebtSeries.data.values.toList(), marker = HMarker(true), showInLegend = true)
        }
    }

}

