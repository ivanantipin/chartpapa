package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*

object Debt2FCFCharter {

    fun makeSeries(debt2fcf : Series<String>, netDebtSeries : Series<String>, title: String): HOptions {
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

            yAxis += EvEbitdaCharter.makeAxis("blue").copy(tickPositions = EvEbitdaCharter.makeTicks(debt2fcf, scale = 2))
            yAxis += EvEbitdaCharter.makeAxis("red").copy(opposite = true)
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1, categories = debt2fcf.data.keys.toList())
            series +=   HSeriesColumn(null ,  "Debt-to-FCF", yAxis = 0, color = "blue", data = debt2fcf.data.values.toList(), marker = HMarker(true), showInLegend = true)
            series +=   HSeriesColumn(null ,  "Net Debt", yAxis = 1, color = "red", data = netDebtSeries.data.values.toList(), marker = HMarker(true), showInLegend = true)
        }
    }

}

