package chart

import com.firelib.techbot.*
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.domain.TimeFrame
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths


enum class BreachType{
    TREND_LINE, DEMARK_SIGNAL
}

object BreachFinder{

    data class HistoricalBreaches(
        val filePath : String
    )

    fun makeSnapFileName(prefix: String, ticker: String, timeFrame: TimeFrame, eventTimeMs: Long) : String{
        val fileName = "snap_${ticker}_${timeFrame}_$eventTimeMs"
        val tempDir = System.getProperty("java.io.tmpdir")
        return Paths.get(tempDir).resolve("${fileName}.png").toFile().absoluteFile.toString()
    }


    fun historicalBreaches(ticker : String, timeFrame: TimeFrame) : HistoricalBreaches {
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, timeFrame.interval)
        val eventTimeMs = targetOhlcs.last().endTime.toEpochMilli()

        return transaction {
            val be =
                BreachEvents.select { BreachEvents.ticker eq ticker and (BreachEvents.timeframe eq timeFrame.name) }
                    .firstOrNull()

            if (be == null) {
                val fileName = makeSnapFileName(BreachType.TREND_LINE.name, ticker, timeFrame, eventTimeMs)
                val conf = BotConfig.getConf(ticker, timeFrame)
                val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)
                val bytes = ChartService.drawLines(lines, targetOhlcs, "${ticker}/${timeFrame}")
                saveFile(bytes, fileName)
                BreachEvents.insert {
                    it[BreachEvents.ticker] = ticker
                    it[BreachEvents.timeframe] = timeFrame.name
                    it[BreachEvents.eventTimeMs] = eventTimeMs
                    it[BreachEvents.photoFile] = fileName
                    it[BreachEvents.eventType] = BreachType.TREND_LINE.name
                }
                return@transaction HistoricalBreaches(filePath = fileName)
            }

            return@transaction HistoricalBreaches(filePath = be[BreachEvents.photoFile])
        }
    }


    fun findBreaches(ticker: String, timeFrame: TimeFrame, breachWindow: Int) : List<BreachEvent>{
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, timeFrame.interval)
        val conf = BotConfig.getConf(ticker, timeFrame)
        val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)
        println("lines count ${lines.size}")
        return lines.filter { it.intersectPoint != null && it.intersectPoint!!.first >= targetOhlcs.size - breachWindow  }.map {
            val endTime = targetOhlcs[it.intersectPoint!!.first].endTime
            val fileName = makeSnapFileName(BreachType.TREND_LINE.name, ticker, timeFrame, targetOhlcs.last().endTime.toEpochMilli())
            saveFile(ChartService.drawLines(listOf(it), targetOhlcs, ticker), fileName)
            BreachEvent(BreachEventKey(ticker, timeFrame, endTime.toEpochMilli(), BreachType.TREND_LINE), fileName)
        }
    }

}