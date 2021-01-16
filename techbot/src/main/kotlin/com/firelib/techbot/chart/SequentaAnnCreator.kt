package com.firelib.techbot.chart

import com.firelib.techbot.BotHelper
import com.firelib.techbot.chart.ChartCreator.makeBuySellPoint
import com.firelib.techbot.chart.ChartCreator.markLevel
import com.firelib.techbot.chart.domain.*
import com.firelib.techbot.initDatabase
import com.funstat.domain.HLine
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.Signal
import firelib.indicators.sequenta.SignalType
import firelib.indicators.sequenta.calcStop
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object SequentaAnnCreator {

    internal val displayedCounts = arrayOf(11, 12)

    fun formatDbl(dbl: Double): String {
        return DecimalFormat("#.##").format(dbl)
    }

    fun genSignals(ohlcs: List<Ohlc>): List<Pair<Int, Signal>> {
        val sequenta = Sequenta()
        return ohlcs.flatMapIndexed { idx, oh ->
            sequenta.onOhlc(oh).filter {
                !(it.type == SignalType.Signal && it.reference.completedSignal == 21)
            }.map { Pair(idx, it) }
        }
    }


    fun createAnnotations(ohlcs: List<Ohlc>): SequentaAnnnotations {

        val labels = ArrayList<HLabel>()
        val shapes = ArrayList<HShape>()
        val lines = ArrayList<HLine>()
        val lines0 = ArrayList<HLine>()

        val curLine = AtomicInteger(0)

        val sequenta = Sequenta(arrayOf(13))

        ohlcs.forEachIndexed { ci, oh ->
            sequenta.onOhlc(oh).forEach { s ->
                val level = if (s.reference.up) oh.high else oh.low
                val point = HPoint(x = oh.endTime.toEpochMilli(), y = level, xAxis = 0, yAxis = 0)
                val base = HLabel(
                    point = point,
                    drawOnTop = s.reference.up,
                    backgroundColor = "rgba(255,255,255,0)", //if (s.reference.up) "red" else "green",
                    borderColor = if (s.reference.up) "rgba(255,0,0,0.5)" else "rgba(0,255,0,0.5)",
                    verticalAlign = if (s.reference.up) "bottom" else "top",
                    distance = if (s.reference.up) 10 else -30,
                    style = HStyle(fontSize = "8px")
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
                            base.copy(text = "+", shape = "connector", style = HStyle(fontSize = "6px"), distance = 6)
                        )
                    }
                    SignalType.Signal -> {

                        val ratio = s.reference.recycleRatio()
                        val recycle = if (ratio != null) "/R=${formatDbl(ratio)}" else "";

                        if (!s.reference.isCancelled && (ratio == null || ratio < 1)) {
                            labels.add(base.copy(text = "${s.reference.completedSignal}" + recycle))

                            if (s.reference.up) {
                                shapes.add(makeBuySellPoint("red", point.x!!, point.y!!, Side.Sell))
                            } else {
                                shapes.add(makeBuySellPoint("green", point.x!!, point.y!!, Side.Buy))
                            }
                            val endOh = ohlcs[Math.min(ci + 3, ohlcs.size - 1)]

                            var hline = HLine(
                                ohlcs[ci - 3].endTime.toEpochMilli(),
                                endOh.endTime.toEpochMilli(),
                                sequenta.calcStop(s.reference.up, s.reference.start, s.idx),
                                dashStyle = "Solid",
                                color = if (s.reference.up) "red" else "green"
                            )
                            lines0.add(hline)
                        }
                    }
                    SignalType.SetupReach -> {
                        val hhline = HLine(
                            s.reference.getStart().toEpochMilli(),
                            s.reference.getEnd().toEpochMilli(),
                            s.reference.tdst,
                            dashStyle = "ShortDash",
                            color = if (s.reference.up) "green" else "red"
                        )
                        labels.add(markLevel(s.reference.getStart().toEpochMilli(), s.reference.tdst, s.reference.up))
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
                                distance = if (s.reference.up) 5 else -15,
                                text = "x",
                                style = HStyle(fontSize = "6px"),
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
        return SequentaAnnnotations(labels, shapes, lines)
    }
}


fun main() {
    initDatabase()
    transaction {
        val ohs = BotHelper.getOhlcsForTf("VEON", Interval.Day).subList(0, 100)
        val ann = SequentaAnnCreator.createAnnotations(ohs)
        ChartService.drawSequenta(ann, ohs, "rtkm")
    }
}