package com.firelib.techbot.sequenta

import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.SignalType
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import firelib.indicators.sequenta.SequentaSignalType
import java.time.Instant

object SequentaSignals : SignalGenerator {

    override fun signalType(): SignalType {
        return SignalType.DEMARK
    }

    override fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        window: Int,
        lastSignalTime : Instant,
        settings: Map<String, String>,
        ohlcs : List<Ohlc>
    ): List<Pair<BreachEventKey,HOptions>> {
        val signals = SequentaAnnCreator.genSignals(ohlcs)

        val ret = signals.
            filter { it.type == SequentaSignalType.Signal && it.idx > ohlcs.size - window && ohlcs[it.idx].endTime > lastSignalTime}
            .map {
            val time = ohlcs[it.idx].endTime
            val key = BreachEventKey(instr.id, tf, time.toEpochMilli(), SignalType.DEMARK)
            key to it
        }

        if(ret.isNotEmpty()){
            val signal = ret.last()
            val opts = SequentaAnnCreator.makeSequentaOpts(SequentaAnnCreator.createAnnotations(signals, ohlcs), ohlcs,
                "Signal: ${makeTitle(tf, instr, settings)}")
            return listOf(signal.first to opts)
        }

        return emptyList()
    }

    override fun drawPicture(
        instr: InstrId,
        tf: TimeFrame, settings: Map<String, String>, ohlcs : List<Ohlc>
    ): HOptions {
        val signals = SequentaAnnCreator.genSignals(ohlcs)
        val annotations = SequentaAnnCreator.createAnnotations(signals, ohlcs)
        return SequentaAnnCreator.makeSequentaOpts(annotations, ohlcs, makeTitle(tf, instr, settings))
    }

    fun makeTitle(timeFrame: TimeFrame, instr: InstrId, settings: Map<String, String>): String {
        return "Sequenta ${instr.code} (${timeFrame}"
    }

}
