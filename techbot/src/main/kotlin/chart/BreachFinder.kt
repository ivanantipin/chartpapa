package chart

import com.firelib.techbot.*
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.domain.TimeFrame
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.core.store.GlobalConstants
import firelib.core.store.MdStorageImpl
import firelib.finam.timeFormatter
import java.time.Instant

object BreachFinder {

    fun makeSnapFileName(prefix: String, ticker: String, timeFrame: TimeFrame, eventTimeMs: Long): String {
        val fileName = "${prefix}_${ticker}_${timeFrame}_$eventTimeMs"
        return GlobalConstants.imgFolder.resolve("${fileName}.png").toFile().absoluteFile.toString()
    }

    fun findNewBreaches(
        ticker: InstrId,
        timeFrame: TimeFrame,
        breachWindow: Int,
        existingBreaches: Set<BreachEventKey>
    ): List<BreachEvent> {
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, timeFrame.interval)
        val conf = BotConfig.getConf(ticker, timeFrame)
        val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)


        return lines.filter { it.intersectPoint != null && it.intersectPoint!!.first >= targetOhlcs.size - breachWindow }
            .groupBy {
                val endTime = targetOhlcs[it.intersectPoint!!.first].endTime
                BreachEventKey(ticker.id, timeFrame, endTime.toEpochMilli(), BreachType.TREND_LINE)
            }
            .filter { !existingBreaches.contains(it.key) }
            .map {
                val key = it.key
                val fileName = makeSnapFileName(
                    BreachType.TREND_LINE.name,
                    ticker.id,
                    timeFrame,
                    it.key.eventTimeMs
                )

                val time = timeFormatter.format(Instant.ofEpochMilli(it.key.eventTimeMs).atMoscow())
                val title = "Breakout for ${ticker.code} (${timeFrame}, time is ${time} msk)"
                val img = ChartService.drawLines(it.value, targetOhlcs, title)
                saveFile(img, fileName)
                BreachEvent(key, fileName)
            }
    }


}


fun main() {
    initDatabase()
    val ticker = InstrId(code = "GMKN", market = "1", source = SourceName.FINAM.name)
    MdStorageImpl().updateMarketData(ticker, Interval.Min10);
    BreachFinder.findNewBreaches(ticker, TimeFrame.D, 5, emptySet())
    //UpdateLevelsSensitivities.updateLevelSenses()

}
