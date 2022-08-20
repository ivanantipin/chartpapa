package com.firelib.techbot.tdline

import chart.BreachType
import chart.SignalType
import com.firelib.techbot.BotHelper
import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.TrendsCreator
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.ChartService.post
import com.firelib.techbot.chart.TrendLinesRenderer
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.initDatabase
import com.firelib.techbot.persistence.BotConfig
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.core.store.MdStorageImpl
import firelib.finam.timeFormatter
import java.time.Instant

object TdLineSignals : SignalGenerator {

    override fun signalType(): SignalType {
        return SignalType.TREND_LINE
    }

    override fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        window: Int,
        existing: Set<BreachEventKey>,
        settings: Map<String, String>
    ): List<BreachEvent> {
        val targetOhlcs = BotHelper.getOhlcsForTf(instr, tf.interval)
        val conf = BotConfig.getConf(instr, tf)
        val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)

        return lines.filter { it.intersectPoint != null && it.intersectPoint!!.first >= targetOhlcs.size - window }
            .groupBy {
                val endTime = targetOhlcs[it.intersectPoint!!.first].endTime
                BreachEventKey(instr.id, tf, endTime.toEpochMilli(), BreachType.TREND_LINE)
            }.filter { !existing.contains(it.key) }.map {
                val key = it.key
                val fileName = BreachEvents.makeSnapFileName(
                    BreachType.TREND_LINE.name, instr.id, tf, it.key.eventTimeMs
                )

                val time = timeFormatter.format(Instant.ofEpochMilli(it.key.eventTimeMs).atMoscow())
                val title = "Signal: ${makeTitle(tf, instr, settings)}"
                val img = post(TrendLinesRenderer.makeTrendLines(targetOhlcs, title, it.value))
                BotHelper.saveFile(img, fileName)
                BreachEvent(key, fileName)
            }
    }

    override fun drawPicture(
        instr: InstrId,
        tf: TimeFrame,  settings: Map<String, String>
    ): HOptions {
        val targetOhlcs = BotHelper.getOhlcsForTf(instr, tf.interval)
        val conf = BotConfig.getConf(instr, tf)
        val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)
        return TrendLinesRenderer.makeTrendLines(targetOhlcs, makeTitle(tf, instr, settings), lines)
    }

    fun makeTitle(timeFrame: TimeFrame, instr: InstrId, settings: Map<String, String>): String {
        return "Trend line ${instr.code} (${timeFrame})"
    }

}

fun main() {
    initDatabase()
    val ticker = InstrId(code = "GMKN", market = "1", source = SourceName.FINAM.name)
    MdStorageImpl().updateMarketData(ticker, Interval.Min10);
    TdLineSignals.checkSignals(ticker, TimeFrame.D, 5, emptySet(), emptyMap())
    //UpdateLevelsSensitivities.updateLevelSenses()

}
