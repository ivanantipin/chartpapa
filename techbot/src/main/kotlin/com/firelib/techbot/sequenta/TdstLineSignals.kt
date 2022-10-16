package com.firelib.techbot.sequenta

import com.firelib.techbot.breachevent.BreachType
import com.firelib.techbot.BotHelper
import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.TechbotApp
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.HorizontalLevelsRenderer
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.mainLogger
import firelib.core.domain.InstrId
import firelib.core.domain.LevelSignal
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.indicators.SR
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.SequentaSignalType
import java.lang.Integer.min

data class TdstResult(val lines: List<SR>, val signals: List<LevelSignal>)

object TdstLineSignals : SignalGenerator {

    fun checkIntersect(level: Double, ohlc: List<Ohlc>, idx: Int, window: Int): Side {
        val last = ohlc[idx]
        val slice = ohlc.slice(idx - window until idx)
        return if (last.high > level && slice.all { it.close <= level && it.open <= level }) {
            if (last.close > level) {
                Side.Buy
            } else {
                Side.Sell
            }
        } else if (last.low < level && slice.all { it.close >= level && it.open >= level }) {
            if (last.close < level) {
                Side.Sell
            } else {
                Side.Buy
            }
        } else {
            Side.None
        }
    }

    fun genSignals(ohlcs: List<Ohlc>): TdstResult {
        val sequenta = Sequenta(arrayOf(13))

        val levels = mutableListOf<Sequenta.Setup>()

        val signals = ohlcs.flatMapIndexed { idx, oh ->
            val refs = sequenta.onOhlc(oh).filter { it.type == SequentaSignalType.SetupReach }
            levels += refs.map { it.reference }

            val signals: List<LevelSignal> = levels.map { lvl ->
                val side = checkIntersect(lvl.tdst, ohlcs, idx, min(idx, 10))
                if (side != Side.None) {
                    LevelSignal(side, oh.endTime.toEpochMilli(), SR(lvl.getStart(), lvl.getEnd(), lvl.tdst))
                } else {
                    null
                }
            }.filterNotNull()
            signals
        }
        return TdstResult(levels.map { SR(it.getStart(), it.getEnd(), it.tdst) }, signals)
    }

    override fun signalType(): com.firelib.techbot.SignalType {
        return com.firelib.techbot.SignalType.TDST
    }

    override fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        window: Int,
        existing: Set<BreachEventKey>,
        settings: Map<String, String>,
        techBotApp: TechbotApp

    ): List<BreachEvent> {

        val targetOhlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval)

        if (targetOhlcs.isEmpty()) {
            mainLogger.info("ohlcs is empty for ${instr}")
            return emptyList()
        }

        val result = genSignals(targetOhlcs)
        val title = makeTitle(tf, instr, settings)
        val options = HorizontalLevelsRenderer().levelBreaches(targetOhlcs, title, result.signals, result.lines)

        val threshold = targetOhlcs[targetOhlcs.size - window - 1].endTime.toEpochMilli()

        val newSignals = result.signals
            .filter { it.time >= threshold }
            .map { BreachEventKey(instr.id, tf, targetOhlcs.last().endTime.toEpochMilli(), BreachType.TDST_SIGNAL) }
            .filter { !existing.contains(it) }



        return if (newSignals.isNotEmpty()) {
            val img = ChartService.post(options)
            newSignals.map {
                val fileName = BreachEvents.makeSnapFileName(
                    BreachType.TDST_SIGNAL.name, instr.id, tf, it.eventTimeMs
                )
                BotHelper.saveFile(img, fileName)
                BreachEvent(it, fileName)
            }
        } else {
            emptyList()
        }
    }

    override fun drawPicture(
        instr: InstrId,
        tf: TimeFrame,
        settings: Map<String, String>,
        techBotApp: TechbotApp
    ): HOptions {
        val targetOhlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval)
        val result = genSignals(targetOhlcs)
        val title = makeTitle(tf, instr, settings)
        val options = HorizontalLevelsRenderer().levelBreaches(targetOhlcs, title, result.signals, result.lines)
        return options
    }

    fun makeTitle(timeFrame: TimeFrame, instr: InstrId, settings: Map<String, String>): String {
        return "TDST tf=${timeFrame} ${instr.code}"
    }
}
