package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*

object OperatingStructureCharter {
    fun makeSeries(ser : List<Series<String>>, title: String, colors : Map<String,String>): HOptions {

        return HOptions(
            title = HTitle(title),
            chart = HChart(margin = listOf(100, 70, 50, 70), type = "column"),
            legend = HLegend(
                floating = false,
                borderWidth = 1,
                borderColor = "#444444",
                verticalAlign = "top",
                layout = "horizontal"
            ),
        ).apply {
            yAxis += HAxis(height = "100%", lineWidth = 1, offset = 10, title = HTitle("Money"))
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1, categories = ser[0].data.keys.toList())
            series +=   ser.map {
                HSeriesColumn(
                    null,
                    it.name,
                    color = colors[it.name],
                    data = it.data.values.toList(),
                    marker = HMarker(true),
                    showInLegend = true
                )
            }
            plotOptions = mutableMapOf<String,Any>().apply {
                this["column"] = mutableMapOf<String,Any>().apply {
                    this["stacking"] = "normal"
                }
            }

        }
    }

}