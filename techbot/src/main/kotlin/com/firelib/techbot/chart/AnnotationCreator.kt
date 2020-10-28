package com.firelib.techbot.chart

import com.firelib.techbot.BotHelper
import com.firelib.techbot.chart.domain.SequentaAnnnotations
import com.firelib.techbot.chart.domain.HLabel
import com.firelib.techbot.chart.domain.HPoint
import com.firelib.techbot.initDatabase
import com.funstat.domain.HLine
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.atUtc
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.SignalType
import firelib.indicators.sequenta.calcStop
import org.jetbrains.exposed.sql.transactions.transaction
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


    fun createAnnotations(ohlcs: List<Ohlc>): SequentaAnnnotations {
        val sequenta = Sequenta()

        val labels = ArrayList<HLabel>()
        val lines = ArrayList<HLine>()
        val lines0 = ArrayList<HLine>()

        val curLine = AtomicInteger(0)

        for (ci in ohlcs.indices) {
            val oh = ohlcs[ci]
            val signals = sequenta.onOhlc(oh)
            signals.forEach { s ->
                val level = if (s.reference.up) oh.high else oh.low
                val point = HPoint(x = oh.endTime.toEpochMilli(), y = level, xAxis = 0, yAxis = 0)
                val base = HLabel(
                    point = point,
                    drawOnTop = s.reference.up,
                    backgroundColor = if (s.reference.up) "red" else "green",
                    verticalAlign = if (s.reference.up) "bottom" else "top",
                    distance = if (s.reference.up) 10 else -30
                )

                when (s.type) {
                    SignalType.Cdn -> {
                        val count = s.reference.countDowns.size
                        if (count == 8 || ci > ohlcs.size - 10 && displayedCounts.contains(count)) {
                            labels.add(
                                base.copy(text = "" + count, shape = "connector")
                            )
                        }
                    }
                    SignalType.Deffered -> if (s.reference.completedSignal < 13) {
                        labels.add(
                            base.copy(text = "+", shape = "connector")
                        )
                    }
                    SignalType.Signal -> {

                        val ratio = s.reference.recycleRatio()
                        val recycle = if (ratio != null) "/R=${formatDbl(ratio)}" else "";

                        labels.add(base.copy(text = "${s.reference.completedSignal}" + recycle))
                        val endOh = ohlcs[Math.min(ci + 3, ohlcs.size - 1)]

                        var hline = HLine(
                            ohlcs[ci - 3].endTime.toEpochMilli(),
                            endOh.endTime.toEpochMilli(),
                            sequenta.calcStop(s.reference.up, s.reference.start, sequenta.data.size),
                            dashStyle = "Solid",
                            color = if (s.reference.up) "red" else "green"
                        )
                        lines0.add(hline)
                    }
                    SignalType.SetupReach -> {
                        val hhline = HLine(s.reference.getStart().toEpochMilli(), s.reference.getEnd().toEpochMilli(), s.reference.tdst,
                            dashStyle = "ShortDash", color= if (s.reference.up) "green" else "red")
                        lines.add(hhline)
                        while (curLine.get() < lines.size - 5) {
                            lines[curLine.get()] = lines[curLine.get()].copy(end = oh.endTime.toEpochMilli())
                            curLine.incrementAndGet()
                        }
                    }
                    SignalType.Flip -> {
                        if (s.reference.up) oh.high else oh.low
                        labels.add(
                            base.copy(
                                text = "" + (ci - s.reference.start + 1),
                                backgroundColor = "white",
                                shape = "circle"
                            )
                        )
                    }
                }
            }
        }
        while (curLine.get() < lines.size) {
            lines[curLine.get()] = lines[curLine.get()].copy(end = ohlcs[ohlcs.size - 1].endTime.toEpochMilli())
            curLine.incrementAndGet()
        }
        lines.addAll(lines0)
        return SequentaAnnnotations(labels, lines)
    }
}


fun main() {
    initDatabase()
    transaction {
        val targetOhlcs = BotHelper.getOhlcsForTf("sber", Interval.Day).subList(0,100)

        val ann = AnnotationCreator.createAnnotations(targetOhlcs)
        println(ann)

        ChartService.drawSequenta(ann, targetOhlcs, "sber")


//        val line = TdLine(0, targetOhlcs.size - 1, targetOhlcs[0].high, targetOhlcs.last().high, Resistance, 0, 0.0)
//        val line1 = TdLine(0, targetOhlcs.size/2, targetOhlcs[0].high, targetOhlcs[targetOhlcs.size/2].high, Resistance, 0, 0.0)
//        val lineS = TdLine(0, targetOhlcs.size - 1, targetOhlcs[0].low, targetOhlcs.last().low, Support, 0, 0.0)
//        ChartService.drawLines(listOf(line,line1,lineS), targetOhlcs, "sber")
    }
}