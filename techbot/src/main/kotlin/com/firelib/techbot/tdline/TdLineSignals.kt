package com.firelib.techbot.tdline

import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.SignalType
import com.firelib.techbot.TrendsCreator
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.chart.TrendLinesRenderer
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.persistence.BotConfig
import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import java.time.Instant

object TdLineSignals : SignalGenerator {

    override fun signalType(): SignalType {
        return SignalType.TREND_LINE
    }

    override fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        window: Int,
        lastSignalTime: Instant,
        settings: Map<String, String>,
        ohlcs: List<Ohlc>
    ): List<Pair<BreachEventKey, HOptions>> {
        val targetOhlcs = ohlcs
        val conf = BotConfig.getConf(instr, tf)
        val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)

        return lines.filter { it.intersectPoint != null && it.intersectPoint!!.first >= targetOhlcs.size - window }
            .groupBy {
                val endTime = targetOhlcs[it.intersectPoint!!.first].endTime
                BreachEventKey(instr.id, tf, endTime.toEpochMilli(), SignalType.TREND_LINE)
            }.filter { it.key.eventTimeMs > lastSignalTime.toEpochMilli() }
            .map {
                val key = it.key
                val title = "Signal: ${makeTitle(tf, instr, settings)}"
                val options = TrendLinesRenderer.makeTrendLines(targetOhlcs, title, it.value)
                key to options
            }
    }

    override fun drawPicture(
        instr: InstrId,
        tf: TimeFrame,
        settings: Map<String, String>,
        ohlcs: List<Ohlc>
    ): HOptions {
        val targetOhlcs = ohlcs
        val conf = BotConfig.getConf(instr, tf)
        val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)
        return TrendLinesRenderer.makeTrendLines(targetOhlcs, makeTitle(tf, instr, settings), lines)
    }

    fun makeTitle(timeFrame: TimeFrame, instr: InstrId, settings: Map<String, String>): String {
        return "Trend line ${instr.code} (${timeFrame})"
    }

}