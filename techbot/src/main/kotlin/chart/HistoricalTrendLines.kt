package chart

import com.firelib.techbot.*
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.ChartService.post
import com.firelib.techbot.chart.TrendLinesRenderer
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.persistence.BotConfig
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object HistoricalTrendLines {
    fun historicalTrendLines(ticker: InstrId, timeFrame: TimeFrame): HistoricalBreaches {
        val eventTimeMs = Interval.Min10.truncTime(System.currentTimeMillis())

        val be = transaction {
            BreachEvents.select {
                BreachEvents.instrId eq ticker.id and
                        (BreachEvents.timeframe eq timeFrame.name) and
                        (BreachEvents.eventType eq BreachType.TREND_LINE_SNAPSHOT.name) and
                        (BreachEvents.eventTimeMs eq eventTimeMs)
            }.firstOrNull()
        }

        return if (be == null) {
            val targetOhlcs = OhlcsService.instance.getOhlcsForTf(ticker, timeFrame.interval)
            val fileName = BreachEvents.makeSnapFileName(BreachType.TREND_LINE.name, ticker.id, timeFrame, eventTimeMs)
            val conf = BotConfig.getConf(ticker, timeFrame)
            val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)
            val bytes = post(
                TrendLinesRenderer.makeTrendLines(
                    targetOhlcs,
                    "Trend lines for ${ticker.code} (${timeFrame})",
                    lines
                )
            )
            BotHelper.saveFile(bytes, fileName)
            updateDatabase("update trend lines events") {
                BreachEvents.insert {
                    it[instrId] = ticker.id
                    it[timeframe] = timeFrame.name
                    it[BreachEvents.eventTimeMs] = eventTimeMs
                    it[photoFile] = fileName
                    it[eventType] = BreachType.TREND_LINE_SNAPSHOT.name
                }
            }
            HistoricalBreaches(filePath = fileName)
        } else {
            HistoricalBreaches(filePath = be[BreachEvents.photoFile])
        }
    }
}

fun main() {
    initDatabase()
    val lines = HistoricalTrendLines.historicalTrendLines(InstrId("RASP", "1"), TimeFrame.H)
    println(lines)
}
