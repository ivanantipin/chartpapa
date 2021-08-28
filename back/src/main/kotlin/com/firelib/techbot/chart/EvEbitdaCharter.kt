package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*
import java.math.BigDecimal
import java.math.RoundingMode

object EvEbitdaCharter {

    fun makeTicks(series: Series<*>, number: Int = 5, scale: Int = 0): List<BigDecimal> {
        val max = series.data.maxOf { it.value }
        val min = series.data.minOf { it.value }
        return (0 until number + 1).map { it * ((max - min) / number) + min }
            .map { BigDecimal.valueOf(it).setScale(scale, RoundingMode.HALF_EVEN) }
    }

    fun makeAxis(color: String): HAxis {
        return HAxis(
            height = "100%", lineWidth = 1, offset = 10,
            title = HTitle("", style = HStyle(color = color)),
            gridLineColor = color, labels = HAxisLabels(HStyle(color = color))
        )
    }

    fun makeSeries(ebitda: Series<String>, ev2ebitda: Series<String>, title: String): HOptions {

        return HOptions(
            title = HTitle(title),
            //top right bottom left
            chart = HChart(margin = listOf(100, 50, 50, 50)),
            legend = HLegend(
                floating = false,
                borderWidth = 1,
                borderColor = "#444444",
                verticalAlign = "top",
                layout = "horizontal"
            )).apply {
            val evColor = "red"
            val ev2ebitdaColor = "blue"
            yAxis += makeAxis(evColor).copy(opposite = false)
            yAxis += makeAxis(ev2ebitdaColor).copy(opposite = true, tickPositions = makeTicks(ev2ebitda))
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1, categories = ebitda.data.keys.toList())
            series += HSeriesColumn(
                null,
                "EV",
                yAxis = 0,
                color = evColor,
                data = ebitda.data.values.toList(),
                marker = HMarker(true),
                showInLegend = true
            )
            series += HSeriesColumn(
                null,
                "EV-to-EBITDA",
                yAxis = 1,
                color = ev2ebitdaColor,
                data = ev2ebitda.data.values.toList(),
                marker = HMarker(true),
                showInLegend = true
            )
        }
    }

}

