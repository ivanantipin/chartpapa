package com.firelib.techbot.sequenta

import chart.BreachFinder
import chart.BreachType
import com.firelib.techbot.*
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
        val sii = signals.filter { it.second.type == SignalType.Signal }
        return signals.flatMap {
            val time = ohlcs.last().endTime
            val key = BreachEventKey(instr.id, tf, time.toEpochMilli(), BreachType.DEMARK_SIGNAL)
            val newSignal =
                it.second.type == SignalType.Signal && it.first > ohlcs.size - window && !existing.contains(key)

            if (newSignal) {
                val img = SequentaAnnCreator.drawSequenta(
                    SequentaAnnCreator.createAnnotations(signals, ohlcs), ohlcs,
                    "Sequenta signal ${instr.code} (${tf}, time is ${timeFormatter.format(time.atMoscow())} msk)"
                )
                val fileName = BreachFinder.makeSnapFileName(
                    BreachType.DEMARK_SIGNAL.name,
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

fun main() {
    initDatabase()
    //val ticker = InstrId(code = "GAZP", market = "1", source = SourceName.FINAM.name)
    val ticker = InstrId(code = "VIST", market = "XNAS", source = SourceName.POLIGON.name)
    MdStorageImpl().updateMarketData(ticker, Interval.Min10);
    SequentaSignals.checkSignals(ticker, TimeFrame.D, 180, emptySet())

}
