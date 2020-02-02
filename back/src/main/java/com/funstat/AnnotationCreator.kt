package com.funstat

import com.funstat.domain.Annotations
import com.funstat.domain.HLine
import com.funstat.domain.Label
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.Signal
import firelib.indicators.sequenta.SignalType
import firelib.common.misc.atUtc
import firelib.core.domain.Ohlc
import firelib.core.domain.range
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object AnnotationCreator {

    internal val displayedCounts = arrayOf(11, 12, 20)

    /*
labelOptions: {
    shape: 'connector',
            align: 'right',
            justify: false,
            crop: true,
            style: {
        fontSize: '0.8em',
                textOutline: '1px white'
    }
},
*/

    fun formatDbl(dbl: Double): String {
        return DecimalFormat("#.##").format(dbl)
    }


    fun createAnnotations(ohlcs: List<Ohlc>): Annotations {
        val sequenta = Sequenta()

        val labels = ArrayList<Label>()
        val lines = ArrayList<HLine>()
        val lines0 = ArrayList<HLine>()

        val curLine = AtomicInteger(0)

        for (ci in ohlcs.indices) {
            val oh = ohlcs[ci]
            val signals = sequenta.onOhlc(oh)
            signals.forEach { s ->

                val base = HashMap<String, String>()

                base["drawOnTop"] = "" + s.reference.up
                base["backgroundColor"] = if (s.reference.up) "red" else "green"
                base["verticalAlign"] = if (s.reference.up) "bottom" else "top"
                base["distance"] = if (s.reference.up) "10" else "-30"

                val level = if (s.reference.up) oh.high else oh.low

                val baseLabel = Label(level, oh.endTime.atUtc(), base)

                when (s.type) {
                    SignalType.Cdn -> {
                        val count = s.reference.countDowns.size
                        if (count == 8 || ci > ohlcs.size - 10 && displayedCounts.contains(count)) {
                            labels.add(baseLabel.withAttribute("text", "" + count)
                                    .withAttribute("shape", "connector")
                            )
                        }
                    }
                    SignalType.Deffered -> if (s.reference.completedSignal < 13) {
                        labels.add(baseLabel.withAttribute("text", "+")
                                .withAttribute("shape", "connector")
                        )
                    }
                    SignalType.Signal -> {

                        val ratio = s.reference.recycleRatio()
                        val recycle = if(ratio != null) "/R=${formatDbl(ratio)}" else "";

                        labels.add(baseLabel.withAttribute("text", "" + s.reference.completedSignal + recycle))
                        val endOh = ohlcs[Math.min(ci + 3, ohlcs.size - 1)]
                        var hline = HLine(ohlcs[ci - 3].endTime.atUtc(), endOh.endTime.atUtc(), calcStopLine(ohlcs, ci, s))


                        hline = hline.withAttribute("color", if (s.reference.up) "red" else "green")
                        hline = hline.withAttribute("dashStyle", "Solid")
                        lines0.add(hline)
                    }
                    SignalType.SetupReach -> {
                        val hhline = HLine(s.reference.getStart(), s.reference.getEnd(), s.reference.tdst)
                                .withAttribute("dashStyle", "ShortDash")
                                .withAttribute("color", if (s.reference.up) "green" else "red")
                        lines.add(hhline)
                        while (curLine.get() < lines.size - 5) {
                            lines[curLine.get()] = lines[curLine.get()].copy(end = oh.endTime.atUtc())
                            curLine.incrementAndGet()
                        }
                    }
                    SignalType.Flip -> {
                        val clevel = if (s.reference.up) oh.high else oh.low
                        labels.add(baseLabel
                                .withAttribute("text", "" + (ci - s.reference.start + 1))
                                .withAttribute("backgroundColor", "white")
                                .withAttribute("shape", "circle")
                        )
                    }
                }
            }
        }
        while (curLine.get() < lines.size) {
            lines[curLine.get()] = lines[curLine.get()].copy(end = ohlcs[ohlcs.size - 1].endTime.atUtc())
            curLine.incrementAndGet()
        }
        lines.addAll(lines0)
        return Annotations(labels, lines)
    }

    private fun calcStopLine(ohlcs: List<Ohlc>, ci: Int, s: Signal): Double {
        var curLevel: Double
        if (s.reference.up) {
            curLevel = java.lang.Double.MIN_VALUE
            for (i in s.reference.start..ci) {
                val ohh = ohlcs[i]
                curLevel = Math.max(curLevel, ohh.high + ohh.range())
            }
        } else {
            curLevel = java.lang.Double.MAX_VALUE
            for (i in s.reference.start until ci) {
                val ohh = ohlcs[i]
                curLevel = Math.min(curLevel, ohh.low - ohh.range())
            }
        }
        return curLevel
    }
}
