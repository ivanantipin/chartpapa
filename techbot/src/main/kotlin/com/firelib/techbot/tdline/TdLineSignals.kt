package com.firelib.techbot.tdline

import chart.BreachType
import com.firelib.techbot.*
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.ChartService.post
import com.firelib.techbot.chart.TrendLinesRenderer
import com.firelib.techbot.domain.TimeFrame
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.core.store.MdStorageImpl
import firelib.finam.timeFormatter
import java.time.Instant

object TdLineSignals {

    fun checkSignals(
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
                val fileName = BreachEvents.makeSnapFileName(
                    BreachType.TREND_LINE.name,
                    ticker.id,
                    timeFrame,
                    it.key.eventTimeMs
                )

                val time = timeFormatter.format(Instant.ofEpochMilli(it.key.eventTimeMs).atMoscow())
                val title = "Breakout for ${ticker.code} (${timeFrame}, time is ${time} msk)"
                val img = post(TrendLinesRenderer.makeTrendLines(targetOhlcs, title, it.value))
                saveFile(img, fileName)
                BreachEvent(key, fileName)
            }
    }


}


fun main() {
    initDatabase()
    val ticker = InstrId(code = "GMKN", market = "1", source = SourceName.FINAM.name)
    MdStorageImpl().updateMarketData(ticker, Interval.Min10);
    TdLineSignals.checkSignals(ticker, TimeFrame.D, 5, emptySet())
    //UpdateLevelsSensitivities.updateLevelSenses()

}
