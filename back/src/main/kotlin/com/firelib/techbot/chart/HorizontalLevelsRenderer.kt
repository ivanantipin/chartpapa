package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.HAnnotation
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.chart.domain.HSeries
import com.funstat.domain.HLine
import firelib.core.domain.LevelSignal
import firelib.core.domain.Ohlc
import firelib.indicators.SR

class HorizontalLevelsRenderer {

    fun level2series(
        lines: List<SR>,
        hours: List<Ohlc>
    ): List<HSeries> {
        return RenderUtils.renderHLines(lines.flatMap {
            val start = it.initial.toEpochMilli()
            val end = it.activeDate.toEpochMilli()
            listOf(
                HLine(start, end, it.level, "solid", "green"),
                HLine(end, hours.last().endTime.toEpochMilli(), it.level, "dash", "green")
            )
        })
    }


    fun makeLevelOptions(
        hours: List<Ohlc>,
        title: String,
        lines: List<SR>
    ): HOptions {
        val options = RenderUtils.makeOptions(hours, title)

        options.series += level2series(lines, hours)

        options.annotations += HAnnotation(lines.map {
            RenderUtils.markLevel(it.initial.toEpochMilli(), it.level, false)
        }, emptyList())
        return options
    }

    fun levelBreaches(
        hours: List<Ohlc>,
        title: String,
        signals: List<LevelSignal>
    ): HOptions {
        val options = RenderUtils.makeOptions(hours, title)
        options.series += level2series(signals.map { it.level }, hours)
        val shapes = signals.map {
            RenderUtils.makeBuySellPoint(RenderUtils.getLineColor(it.side), it.time, it.level.level, it.side)
        }

        val labels = signals.map { sr ->
            RenderUtils.markLevel(sr.level.initial.toEpochMilli(), sr.level.level, false)
        }
        options.annotations += HAnnotation(labels, shapes)
        return options
    }

}