package com.firelib.techbot.chart

import com.firelib.techbot.TdLine
import com.firelib.techbot.chart.ChartCreator.makeOptions
import com.firelib.techbot.chart.domain.*
import com.firelib.techbot.domain.LineType.*
import com.funstat.domain.HLine
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ChartService{

    val client = HttpClient()

    @Serializable
    data class HiRequest(val async : Boolean, val infile : HOptions, val constr : String, val scale : Int)

    val urlString = "http://localhost:7801"

    fun drawSequenta(ann : SequentaAnnnotations, hours: List<Ohlc>, title: String) : ByteArray{

        val series = renderHLines(ann.lines)

        val options = makeOptions(hours, title)

        options.annotations = listOf(HAnnotation(labels = ann.labels, shapes = ann.shapes))

        options.series += series

        val optJson = Json { prettyPrint=true }.encodeToString(HiRequest(async = true, infile = options, constr = "StockChart", 2))

        println(optJson)


        return postJson(optJson)
    }

    private fun renderHLines(lines: List<HLine>): List<HSeries> {
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
                        HMarker(false),
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

    private fun postJson(optJson: String) : ByteArray{
        return runBlocking {
            val imagePath = client.post<String> {
                url(urlString)
                header("Content-Type", "application/json")
                body = optJson
            }
            val url = "$urlString/" + imagePath
            println(url)
            client.get<ByteArray>(url)
        }
    }

    fun drawLines(
        lines: List<TdLine>,
        hours: List<Ohlc>,
        title: String
    ): ByteArray {
        val options = makeOptions(hours, title)
        options.annotations += annotations(lines, hours)

        val series = lines.groupBy { it.lineType }.mapValues { (key, value) ->
            value.asSequence().flatMapIndexed { idx, line ->
                renderLine(line, idx, hours)
            }
        }.values.flatMap { it.toList() }

        options.series += series
        options.series += renderHLines(activeLevels(lines, hours))

        val optJson = Json { prettyPrint=true }.encodeToString(HiRequest(async = true, infile = options, constr = "StockChart", 2))

        return postJson(optJson)
    }

    private fun annotations(lines : List<TdLine>, hours: List<Ohlc>): HAnnotation {
        val shapes = lines.filter { it.intersectPoint != null }.map {
            val color = getLineColor(it)
            val x = hours[it.intersectPoint!!.first].endTime.toEpochMilli()
            val y = it.intersectPoint!!.second
            val side = if (it.lineType == Support) Side.Sell else Side.Buy
            ChartCreator.makeBuySellPoint(color, x, y, side)
        }
        return HAnnotation(emptyList(), shapes)
    }

    private fun getLineColor(it: TdLine): String {
        val color = if (it.lineType == Support) "red" else "green"
        return color
    }

    private fun activeLevels(lines : List<TdLine>, hours: List<Ohlc>): List<HLine> {
        return lines.filter { it.intersectPoint == null }.map {
            val start = hours[0].endTime.toEpochMilli()
            val end = hours[20].endTime.toEpochMilli()
            HLine(start, end, it.calcValue(hours.size - 1), "solid", getLineColor(it))
        }
    }


    private fun renderLine(
        line: TdLine,
        idx: Int,
        hours: List<Ohlc>
    ): Sequence<HSeries> {
        val color = if (line.lineType == Support) "green" else "red"

        var name = "$idx"
        var showInLegend = false

        if (idx == 0) {
            name = if (line.lineType == Resistance) "resistance" else "support"
            showInLegend = true
        }

        return sequence {
            val data : List<Array<Double>> = listOf(
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

            val data2nd : List<Array<Double>> = if (line.intersectPoint != null) {
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
}