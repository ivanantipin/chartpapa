package com.github.kotlintelegrambot.echo.chart

import chart.ChartCreator.makeOptions
import chart.HMarker
import chart.HOptions
import chart.HSeries
import com.firelib.sub.BreachEvents
import com.firelib.techbot.initDatabase
import com.firelib.techbot.saveFile
import com.firelib.trend.BotConfig
import com.firelib.trend.TrendsCreator
import com.firelib.trend.TrendsCreator.findRegresLines
import com.github.kotlintelegrambot.echo.com.firelib.telbot.BreachEvent
import com.github.kotlintelegrambot.echo.com.firelib.telbot.BreachEventKey
import firelib.telbot.TimeFrame
import com.github.kotlintelegrambot.echo.com.firelib.trend.TdLine
import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.store.MdDaoContainer
import firelib.iqfeed.IntervalTransformer
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths
import java.time.LocalDateTime

object ImageService{

    val client = HttpClient()

    @Serializable
    data class HiRequest(val async : Boolean, val infile : HOptions, val constr : String, val scale : Int)

    val urlString = "http://localhost:7801"

    fun getOhlcsForTf(ticker : String, timeFrame: Interval) : List<Ohlc>{
        val startTime = LocalDateTime.now().minus(timeFrame.duration.multipliedBy(BotConfig.window))

        val ohlcs = MdDaoContainer().getDao(SourceName.FINAM, Interval.Min10).queryAll(ticker, startTime)

        return IntervalTransformer.transform(timeFrame, ohlcs)

    }

    fun findBreaches(ticker : String, timeFrame : TimeFrame) : List<BreachEvent>{
        val targetOhlcs = getOhlcsForTf(ticker, timeFrame.interval)
        val conf = BotConfig.getConf(ticker, timeFrame)
        val lines = findRegresLines(targetOhlcs, conf)
        return lines.filter { it.intersectPoint != null && it.intersectPoint!!.first > targetOhlcs.size - 2 }.map {
            val endTime = targetOhlcs[it.intersectPoint!!.first].endTime
            val fileName = makeSnapFileName(ticker, timeFrame, targetOhlcs.last().endTime.toEpochMilli())
            saveFile(drawLines(listOf(it), targetOhlcs, ticker), fileName)
            BreachEvent(BreachEventKey(ticker, timeFrame, endTime.toEpochMilli()), fileName)
        }
    }

    data class HistoricalBreaches(
        val filePath : String
    )

    fun makeSnapFileName(ticker: String, timeFrame  : TimeFrame, eventTimeMs : Long) : String{
        val fileName = "snap_${ticker}_${timeFrame}_$eventTimeMs"
        val tempDir = System.getProperty("java.io.tmpdir")
        return Paths.get(tempDir).resolve("${fileName}.png").toFile().absoluteFile.toString()
    }

    fun historicalBreaches(ticker : String, timeFrame: TimeFrame) : HistoricalBreaches{
        val targetOhlcs = getOhlcsForTf(ticker, timeFrame.interval)
        val eventTimeMs = targetOhlcs.last().endTime.toEpochMilli()

        return transaction {
            val be = BreachEvents.select { BreachEvents.ticker eq ticker and (BreachEvents.timeframe eq timeFrame.name) }
                .firstOrNull()

            if(be == null){
                val fileName = makeSnapFileName(ticker, timeFrame, eventTimeMs)
                val conf = BotConfig.getConf(ticker, timeFrame)
                val lines = findRegresLines(targetOhlcs, conf)
                val bytes = drawLines(lines, targetOhlcs, ticker)
                saveFile(bytes, fileName)
                BreachEvents.insert {
                    it[BreachEvents.ticker] = ticker
                    it[BreachEvents.timeframe] = timeFrame.name
                    it[BreachEvents.eventTimeMs] = eventTimeMs
                    it[BreachEvents.photoFile] = fileName
                }
                return@transaction HistoricalBreaches(filePath = fileName)
            }

            return@transaction HistoricalBreaches(filePath = be[BreachEvents.photoFile])
        }
    }

    private fun drawLines(
        lines: List<TdLine>,
        hours: List<Ohlc>,
        ticker: String
    ): ByteArray {
        val series = lines.asSequence().flatMapIndexed { idx, line ->

            val color = if(line.lineType == TrendsCreator.LineType.Support) "green" else "red"

            sequence<HSeries> {
                val data = listOf(
                    arrayOf(hours[line.x0].endTime.toEpochMilli().toDouble(), line.y0),
                    arrayOf(hours[line.x1].endTime.toEpochMilli().toDouble(), line.y1)
                )
                yield(
                    HSeries(
                        "line",
                        name = "${idx}",
                        HMarker(true),
                        data,
                        showInLegend = true,
                        color = color,
                        dashStyle = "dash",
                        lineWidth = 1
                    )
                )

                val data2nd = if (line.intersectPoint != null) {
                    listOf(
                        arrayOf(hours[line.x1].endTime.toEpochMilli().toDouble(), line.y1),
                        arrayOf(
                            hours[line.intersectPoint!!.first].endTime.toEpochMilli().toDouble(),
                            line.intersectPoint!!.second
                        )
                    )
                }else{
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
                        name = "${idx}_inters",
                        HMarker(true),
                        data2nd,
                        showInLegend = true,
                        color = color,
                        dashStyle = "dot"
                    )
                )
            }
        }

        val options = makeOptions(hours, ticker)

        options.series += series

        val optJson = Json.encodeToString(HiRequest(async = true, infile = options, constr = "StockChart", 2))

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

}

object SensitivityConfig: Table() {
    val ticker = varchar("ticker", 10)
    val pivotOrder = integer("pivot_order")
    val rSquare = double("r_square")
    val timeframe = varchar("timeframe", 10).default(TimeFrame.H.name)

    override val primaryKey = PrimaryKey(ticker, timeframe, name = "sens_conf_pk")
}

fun main() {
    initDatabase()
    transaction {
        ImageService.historicalBreaches("sber", TimeFrame.D)
    }
}