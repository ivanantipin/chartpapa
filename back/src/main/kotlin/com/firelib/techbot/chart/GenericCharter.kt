package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random


data class SeriesUX(val series: Series<String>, val color: String, val yAxis: Int, val type : String = "column", val makeTicks : Boolean = false)

object GenericCharter {

    fun makeTicks(series: Series<*>, number: Int = 5, scale: Int = 5): List<BigDecimal> {
        val max = series.data.maxOf { it.value }
        val min = series.data.minOf { it.value }
        return (0 until number + 1).map { it * ((max - min) / number) + min }
            .map { BigDecimal.valueOf(it).setScale(scale, RoundingMode.HALF_EVEN) }
    }


    fun makeSeries(serI: List<SeriesUX>, title: String, yAxises: List<String>): HOptions {

        val ser = serI.sortedBy { if(it.type == "column") 0 else 1 }

        fun color(idx: Int): String {
            return ser.find { it.yAxis == idx }!!.color
        }

        fun ticks(idx: Int): List<BigDecimal>? {
            val find = ser.find { it.yAxis == idx && it.makeTicks }
            return if(find != null){
                makeTicks(find.series)
            }else{
                null
            }
        }


        return HOptions(
            title = HTitle(title),
            chart = HChart(margin = listOf(100, 70, 50, 70), type = null),
            legend = HLegend(
                floating = false,
                borderWidth = 1,
                borderColor = "#444444",
                verticalAlign = "top",
                layout = "horizontal"
            ),
        ).apply {
            yAxis += yAxises.mapIndexed { idx, title ->
                HAxis(
                    height = "100%", lineWidth = 1, offset = 10,
                    title = HTitle(title, style = HStyle(color = color(idx))),
                    gridLineColor = color(idx), labels = HAxisLabels(HStyle(color = color(idx))), opposite = idx > 0, tickPositions = ticks(idx)
                )
            }


            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1, categories = ser[0].series.data.keys.toList())
            series += ser.map {
                HSeriesColumn(
                    it.type,
                    it.series.name,
                    color = it.color,
                    data = it.series.data.values.toList(),
                    marker = HMarker(true),
                    showInLegend = true,
                    yAxis = it.yAxis
                )
            }
            plotOptions = mutableMapOf<String, Any>().apply {
                this["column"] = mutableMapOf<String, Any>().apply {
                    this["stacking"] = "normal"
                }
            }

        }
    }

}

fun makeSer(name: String): Series<String> {
    val scale = Random(System.nanoTime()).nextDouble() * 5
    val data = (0..5).associateBy({ "$it" }, {
        Random(System.nanoTime()).nextDouble()*scale
    })
    return Series(name, data)
}

