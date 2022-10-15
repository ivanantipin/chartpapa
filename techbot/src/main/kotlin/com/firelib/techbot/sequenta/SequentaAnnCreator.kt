package com.firelib.techbot.sequenta

import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.RenderUtils
import com.firelib.techbot.chart.RenderUtils.makeBuySellPoint
import com.firelib.techbot.chart.RenderUtils.markLevel
import com.firelib.techbot.chart.domain.*
import com.funstat.domain.HLine
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.Signal
import firelib.indicators.sequenta.SequentaSignalType
import firelib.indicators.sequenta.calcStop
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicInteger

data class SignalEvent(
    val idx: Int,
    val up: Boolean,
    val type: SequentaSignalType,
    val setupStart: Int,
    val setupEnd: Int,
    val tdst: Double,
    val cntdn: Int,
    val completedSignal: Int,
    val recycleRatio: Double?
) {
    companion object {
        fun convert(signal: Signal): SignalEvent {
            return SignalEvent(
                signal.idx, signal.reference.up, signal.type,
                signal.reference.start,
                signal.reference.end,
                signal.reference.tdst,
                signal.reference.countDowns.size,
                signal.reference.completedSignal,
                signal.reference.recycleRatio()
            )
        }
    }
}

object SequentaAnnCreator {

    internal val displayedCounts = arrayOf(11, 12)

    fun formatDbl(dbl: Double): String {
        return DecimalFormat("#.##").format(dbl)
    }

    fun genSignals(ohlcs: List<Ohlc>): List<SignalEvent> {
        val sequenta = Sequenta(arrayOf(13))
        return ohlcs.flatMapIndexed { idx, oh ->
            sequenta.onOhlc(oh).map { SignalEvent.convert(it) }
        }
    }

    fun drawSequenta(ann: SequentaAnnnotations, hours: List<Ohlc>, title: String): ByteArray {
        return ChartService.post(makeSequentaOpts(ann, hours, title))
    }

    fun makeSequentaOpts(
        ann: SequentaAnnnotations,
        hours: List<Ohlc>,
        title: String
    ): HOptions {
        val series = RenderUtils.renderHLines(ann.lines)
        val options = RenderUtils.makeOptions(hours, title)
        options.annotations = listOf(HAnnotation(labels = ann.labels, shapes = ann.shapes))
        options.series += series
        return options
    }

    fun createAnnotations(signals: List<SignalEvent>, ohlcs: List<Ohlc>): SequentaAnnnotations {

        val labels = ArrayList<HLabel>()
        val shapes = ArrayList<HShape>()
        val lines = ArrayList<HLine>()
        val lines0 = ArrayList<HLine>()

        val curLine = AtomicInteger(0)

        signals.forEach { s ->
            val oh = ohlcs[s.idx]
            val level = if (s.up) oh.high else oh.low
            val point = HPoint(x = oh.endTime.toEpochMilli(), y = level, xAxis = 0, yAxis = 0)
            val base = HLabel(
                point = point,
                drawOnTop = s.up,
                backgroundColor = "rgba(255,255,255,0)", //if (s.reference.up) "red" else "green",
                borderColor = if (s.up) "rgba(255,0,0,0.5)" else "rgba(0,255,0,0.5)",
                verticalAlign = if (s.up) "bottom" else "top",
                distance = if (s.up) 10 else -30,
                style = HStyle(fontSize = "8px")
            )

            when (s.type) {
                SequentaSignalType.Cdn -> {
                    val count = s.cntdn
                    if (count == 8 || displayedCounts.contains(count)) {
                        labels.add(
                            base.copy(text = "" + count, shape = "connector")
                        )
                    }
                }
                SequentaSignalType.Deffered -> if (s.completedSignal < 13) {
                    labels.add(
                        base.copy(text = "+", shape = "connector", style = HStyle(fontSize = "6px"), distance = 6)
                    )
                }
                SequentaSignalType.Signal -> {

                    val ratio = s.recycleRatio
                    val recycle = if (ratio != null) "/R=${formatDbl(ratio)}" else "";

                    if (ratio == null || ratio < 20) {
                        labels.add(base.copy(text = "${s.completedSignal}" + recycle))

                        if (s.up) {
                            val color = if (ratio != null && ratio > 1) "pink" else "red"
                            shapes.add(makeBuySellPoint(color, point.x!!, point.y!!, Side.Sell))
                        } else {
                            val color = if (ratio != null && ratio > 1) "lightgreen" else "green"
                            shapes.add(makeBuySellPoint(color, point.x!!, point.y!!, Side.Buy))
                        }
                        val endOh = ohlcs[Math.min(s.idx + 3, ohlcs.size - 1)]

                        val hline = HLine(
                            ohlcs[s.idx - 3].endTime.toEpochMilli(),
                            endOh.endTime.toEpochMilli(),
                            ohlcs.calcStop(s.up, s.setupStart, s.idx),
                            dashStyle = "Solid",
                            color = if (s.up) "red" else "green"
                        )
                        lines0.add(hline)
                    }
                }
                SequentaSignalType.SetupReach -> {
                    val hhline = HLine(
                        ohlcs[s.setupStart].endTime.toEpochMilli(),
                        ohlcs[s.setupEnd].endTime.toEpochMilli(),
                        s.tdst,
                        dashStyle = "ShortDash",
                        color = if (s.up) "green" else "red"
                    )
                    labels.add(markLevel(ohlcs[s.setupStart].endTime.toEpochMilli(), s.tdst, s.up))
                    lines.add(hhline)
                    while (curLine.get() < lines.size - 5) {
                        lines[curLine.get()] = lines[curLine.get()].copy(end = oh.endTime.toEpochMilli())
                        curLine.incrementAndGet()
                    }
                }
                SequentaSignalType.Flip -> {
                    if (s.up) oh.high else oh.low
                    labels.add(
                        base.copy(
                            distance = if (s.up) 5 else -15,
                            text = "x",
                            style = HStyle(fontSize = "6px"),
                            shape = "circle"
                        )
                    )
                }
                else -> {}
            }
        }
        while (curLine.get() < lines.size) {
            lines[curLine.get()] = lines[curLine.get()].copy(end = ohlcs.last().endTime.toEpochMilli())
            curLine.incrementAndGet()
        }
        lines.addAll(lines0)
        return SequentaAnnnotations(labels, shapes, lines)
    }
}