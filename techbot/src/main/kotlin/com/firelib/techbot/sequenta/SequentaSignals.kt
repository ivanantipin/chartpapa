package com.firelib.techbot.sequenta

import com.firelib.techbot.breachevent.BreachType
import com.firelib.techbot.BotHelper
import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.SignalType
import com.firelib.techbot.TechbotApp
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId

object SequentaSignals : SignalGenerator {

    override fun signalType(): SignalType {
        return SignalType.DEMARK
    }

    override fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        window: Int,
        existing: Set<BreachEventKey>,
        settings: Map<String, String>,
        techBotApp: TechbotApp
    ): List<BreachEvent> {
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval)
        val signals = SequentaAnnCreator.genSignals(ohlcs)

        return signals.flatMap {
            val time = ohlcs.last().endTime
            val key = BreachEventKey(instr.id, tf, time.toEpochMilli(), BreachType.DEMARK_SIGNAL)
            val newSignal = it.type == firelib.indicators.sequenta.SequentaSignalType.Signal && it.idx > ohlcs.size - window && !existing.contains(key)

            if (newSignal) {
                val img = SequentaAnnCreator.drawSequenta(
                    SequentaAnnCreator.createAnnotations(signals, ohlcs), ohlcs,
                    "Signal: ${makeTitle(tf, instr, settings)}"
                )
                val fileName = BreachEvents.makeSnapFileName(
                    BreachType.DEMARK_SIGNAL.name,
                    instr.id,
                    tf,
                    time.toEpochMilli()
                )
                BotHelper.saveFile(img, fileName)
                listOf(BreachEvent(key, fileName))
            } else {
                emptyList()
            }
        }
    }

    override fun drawPicture(
        instr: InstrId,
        tf: TimeFrame, settings: Map<String, String>, techBotApp: TechbotApp
    ): HOptions {
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval)
        val signals = SequentaAnnCreator.genSignals(ohlcs)
        val annotations = SequentaAnnCreator.createAnnotations(signals, ohlcs)
        return SequentaAnnCreator.makeSequentaOpts(annotations, ohlcs, makeTitle(tf, instr, settings))
    }

    fun makeTitle(timeFrame: TimeFrame, instr: InstrId, settings: Map<String, String>): String {
        return "Sequenta ${instr.code} (${timeFrame}"
    }

}
