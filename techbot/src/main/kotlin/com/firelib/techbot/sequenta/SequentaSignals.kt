package com.firelib.techbot.sequenta

import chart.BreachType
import com.firelib.techbot.*
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.domain.TimeFrame
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.atMoscow
import firelib.core.store.MdStorageImpl
import firelib.finam.timeFormatter
import firelib.indicators.sequenta.SignalType

object SequentaSignals {

    fun checkSignals(instr: InstrId, tf: TimeFrame, window: Int, existing: Set<BreachEventKey>): List<BreachEvent> {
        val ohlcs = BotHelper.getOhlcsForTf(instr, tf.interval)
        val signals = SequentaAnnCreator.genSignals(ohlcs)

        val img = SequentaAnnCreator.drawSequenta(
            SequentaAnnCreator.createAnnotations(signals, ohlcs), ohlcs,
            "Sequenta signal ${instr.code} (${tf}, time is  msk)"
        )


        return signals.flatMap {
            val time = ohlcs.last().endTime
            val key = BreachEventKey(instr.id, tf, time.toEpochMilli(), BreachType.DEMARK_SIGNAL)
            val newSignal = it.type == SignalType.Signal && it.idx > ohlcs.size - window && !existing.contains(key)

            if (newSignal) {
                val img = SequentaAnnCreator.drawSequenta(
                    SequentaAnnCreator.createAnnotations(signals, ohlcs), ohlcs,
                    "Sequenta signal ${instr.code} (${tf}, time is ${timeFormatter.format(time.atMoscow())} msk)"
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
}

fun main() {
    initDatabase()
    //val ticker = InstrId(code = "GAZP", market = "1", source = SourceName.FINAM.name)
    val ticker = InstrId(code = "HAL", market = "XNYS", source = SourceName.POLIGON.name)
    //MdStorageImpl().updateMarketData(ticker, Interval.Min10);
    SequentaSignals.checkSignals(ticker, TimeFrame.D, 180, emptySet())

}
