package chart

import com.firelib.techbot.*
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.SequentaAnnCreator
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object HistoricalTrendLines{
    fun historicalTrendLines(ticker: InstrId, timeFrame: TimeFrame): HistoricalBreaches {
        val eventTimeMs = Interval.Min10.truncTime(System.currentTimeMillis())

        return transaction {
            val be =
                BreachEvents.select { BreachEvents.ticker eq ticker.codeAndExch() and
                        (BreachEvents.timeframe eq timeFrame.name) and
                        (BreachEvents.eventType eq BreachType.TREND_LINE_SNAPSHOT.name) and
                        (BreachEvents.eventTimeMs eq eventTimeMs)}
                    .firstOrNull()

            if (be == null) {
                val targetOhlcs = BotHelper.getOhlcsForTf(ticker, timeFrame.interval)
                val fileName = BreachFinder.makeSnapFileName(BreachType.TREND_LINE.name, ticker.codeAndExch(), timeFrame, eventTimeMs)
                val conf = BotConfig.getConf(ticker, timeFrame)
                val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)
                val bytes = ChartService.drawLines(lines, targetOhlcs, "Trend lines for ${ticker.code} (${timeFrame})")
                saveFile(bytes, fileName)
                updateDatabase("update trend lines events") {
                    BreachEvents.insert {
                        it[BreachEvents.ticker] = ticker.codeAndExch()
                        it[BreachEvents.timeframe] = timeFrame.name
                        it[BreachEvents.eventTimeMs] = eventTimeMs
                        it[BreachEvents.photoFile] = fileName
                        it[BreachEvents.eventType] = BreachType.TREND_LINE_SNAPSHOT.name
                    }
                }.get()
                HistoricalBreaches(filePath = fileName)
            } else {
                HistoricalBreaches(filePath = be[BreachEvents.photoFile])
            }
        }
    }
}

fun main() {
    initDatabase()
    val lines = HistoricalTrendLines.historicalTrendLines( InstrId("RASP","1"), TimeFrame.H)
    println(lines)
}
