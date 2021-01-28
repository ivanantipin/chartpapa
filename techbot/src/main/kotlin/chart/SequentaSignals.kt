package chart

import com.firelib.techbot.BotHelper
import com.firelib.techbot.BreachEvent
import com.firelib.techbot.BreachEventKey
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.SequentaAnnCreator
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.saveFile
import firelib.core.domain.InstrId
import firelib.core.misc.atMoscow
import firelib.finam.timeFormatter
import firelib.indicators.sequenta.SignalType

object SequentaSignals {

    fun checkSignals(instr: InstrId, tf: TimeFrame, window: Int, existing: Set<BreachEventKey>): List<BreachEvent> {
        val ohlcs = BotHelper.getOhlcsForTf(instr, tf.interval)
        val signals = SequentaAnnCreator.genSignals(ohlcs)
        return signals.flatMap {
            val time = ohlcs.last().endTime
            val key =
                BreachEventKey(instr.id, tf, time.toEpochMilli(), BreachType.DEMARK_SIGNAL)
            val newSignal =
                it.second.type == SignalType.Signal && it.first > ohlcs.size - window && !existing.contains(key)

            if (newSignal) {
                val img = ChartService.drawSequenta(
                    SequentaAnnCreator.createAnnotations(ohlcs), ohlcs,
                    "Sequenta signal ${instr.code} (${tf}, time is ${timeFormatter.format(time.atMoscow())} msk)"
                )
                val fileName = BreachFinder.makeSnapFileName(
                    BreachType.TREND_LINE.name,
                    instr.id,
                    tf,
                    time.toEpochMilli()
                )
                saveFile(img, fileName)
                listOf(BreachEvent(key, fileName))
            } else {
                emptyList()
            }
        }
    }
}