package chart

import com.firelib.techbot.*
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.Interval
import firelib.indicators.SRMaker
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths


enum class BreachType {
    TREND_LINE, DEMARK_SIGNAL, TREND_LINE_SNAPSHOT, LEVELS_SNAPSHOT
}

object BreachFinder {

    data class HistoricalBreaches(
        val filePath: String
    )

    fun makeSnapFileName(prefix: String, ticker: String, timeFrame: TimeFrame, eventTimeMs: Long): String {
        val fileName = "${prefix}_${ticker}_${timeFrame}_$eventTimeMs"
        val tempDir = System.getProperty("java.io.tmpdir")
        return Paths.get(tempDir).resolve("${fileName}.png").toFile().absoluteFile.toString()
    }

    fun historicalLevels(ticker: String): HistoricalBreaches {
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, Interval.Min10, 4000)
        val eventTimeMs = targetOhlcs.last().endTime.toEpochMilli()

        val maker = SRMaker(100, 2, 0.01)

        return transaction {
            val be =
                BreachEvents.select { BreachEvents.ticker eq ticker and (BreachEvents.timeframe eq TimeFrame.M30.name) and (BreachEvents.eventType eq  BreachType.LEVELS_SNAPSHOT.name)}
                    .firstOrNull()

            if (be == null || true) {
                val fileName = makeSnapFileName(BreachType.LEVELS_SNAPSHOT.name, ticker, TimeFrame.M30, eventTimeMs)

                targetOhlcs.forEach { maker.addOhlc(it) }

                val bytes = ChartService.drawLevels(maker.currentLevels, targetOhlcs, "Levels for ${ticker} ")
                saveFile(bytes, fileName)
                BreachEvents.insert {
                    it[BreachEvents.ticker] = ticker
                    it[BreachEvents.timeframe] = TimeFrame.M30.name
                    it[BreachEvents.eventTimeMs] = eventTimeMs
                    it[BreachEvents.photoFile] = fileName
                    it[BreachEvents.eventType] = BreachType.LEVELS_SNAPSHOT.name
                }
                return@transaction HistoricalBreaches(filePath = fileName)
            }

            return@transaction HistoricalBreaches(filePath = be[BreachEvents.photoFile])
        }
    }


    fun historicalBreaches(ticker: String, timeFrame: TimeFrame): HistoricalBreaches {
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, timeFrame.interval)
        val eventTimeMs = targetOhlcs.last().endTime.toEpochMilli()

        return transaction {
            val be =
                BreachEvents.select { BreachEvents.ticker eq ticker and (BreachEvents.timeframe eq timeFrame.name) and (BreachEvents.eventType eq  BreachType.TREND_LINE_SNAPSHOT.name)}
                    .firstOrNull()

            if (be == null) {
                val fileName = makeSnapFileName(BreachType.TREND_LINE.name, ticker, timeFrame, eventTimeMs)
                val conf = BotConfig.getConf(ticker, timeFrame)
                val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)
                val bytes = ChartService.drawLines(lines, targetOhlcs, "Trend lines for ${ticker} (${timeFrame})")
                saveFile(bytes, fileName)
                BreachEvents.insert {
                    it[BreachEvents.ticker] = ticker
                    it[BreachEvents.timeframe] = timeFrame.name
                    it[BreachEvents.eventTimeMs] = eventTimeMs
                    it[BreachEvents.photoFile] = fileName
                    it[BreachEvents.eventType] = BreachType.TREND_LINE_SNAPSHOT.name
                }
                return@transaction HistoricalBreaches(filePath = fileName)
            }

            return@transaction HistoricalBreaches(filePath = be[BreachEvents.photoFile])
        }
    }


    fun findNewBreaches(
        ticker: String,
        timeFrame: TimeFrame,
        breachWindow: Int,
        existingBreaches: Set<BreachEventKey>
    ): List<BreachEvent> {
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, timeFrame.interval)
        val conf = BotConfig.getConf(ticker, timeFrame)
        val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)
        println("lines count ${lines.size}")
        return lines.filter { it.intersectPoint != null && it.intersectPoint!!.first >= targetOhlcs.size - breachWindow }
            .flatMap {
                val endTime = targetOhlcs[it.intersectPoint!!.first].endTime
                val key = BreachEventKey(ticker, timeFrame, endTime.toEpochMilli(), BreachType.TREND_LINE)
                if (!existingBreaches.contains(key)) {
                    val fileName = makeSnapFileName(
                        BreachType.TREND_LINE.name,
                        ticker,
                        timeFrame,
                        targetOhlcs.last().endTime.toEpochMilli()
                    )
                    val title = "Breakout for ${ticker} (${timeFrame})"
                    val img = ChartService.drawLines(listOf(it), targetOhlcs, title)
                    saveFile(img, fileName)
                    listOf(BreachEvent(key, fileName))
                } else {
                    emptyList()
                }
            }
    }

}