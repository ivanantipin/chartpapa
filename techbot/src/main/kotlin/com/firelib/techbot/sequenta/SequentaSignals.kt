package com.firelib.techbot.sequenta

import chart.BreachType
import com.firelib.techbot.BotHelper
import com.firelib.techbot.staticdata.OhlcsService
import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.TechBotApp
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId
import firelib.indicators.sequenta.SignalType

object SequentaSignals : SignalGenerator {

    override fun signalType(): chart.SignalType {
        return chart.SignalType.DEMARK
    }

    override fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        window: Int,
        existing: Set<BreachEventKey>,
        settings: Map<String, String>,
        techBotApp: TechBotApp
    ): List<BreachEvent> {
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval).value
        val signals = SequentaAnnCreator.genSignals(ohlcs)

        return signals.flatMap {
            val time = ohlcs.last().endTime
            val key = BreachEventKey(instr.id, tf, time.toEpochMilli(), BreachType.DEMARK_SIGNAL)
            val newSignal = it.type == SignalType.Signal && it.idx > ohlcs.size - window && !existing.contains(key)

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
        tf: TimeFrame, settings: Map<String, String>,techBotApp: TechBotApp
    ): HOptions {
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval).value
        val signals = SequentaAnnCreator.genSignals(ohlcs)
        val annotations = SequentaAnnCreator.createAnnotations(signals, ohlcs)
        return SequentaAnnCreator.makeSequentaOpts(annotations, ohlcs, makeTitle(tf, instr, settings))
    }

    fun makeTitle(timeFrame: TimeFrame, instr: InstrId, settings: Map<String, String>): String {
        return "Sequenta ${instr.code} (${timeFrame}"
    }

}
