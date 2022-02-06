package com.firelib.techbot.macd

import chart.BreachType
import com.firelib.techbot.*
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents.makeSnapFileName
import com.firelib.techbot.chart.RenderUtils
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.domain.*
import com.firelib.techbot.command.RsiBolingerCommand
import com.firelib.techbot.command.RsiBolingerCommand.Companion.RSI_HIGH_ATTR
import com.firelib.techbot.command.RsiBolingerCommand.Companion.RSI_LOW_ATTR
import com.firelib.techbot.domain.TimeFrame
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.core.store.MdStorageImpl
import firelib.indicators.Rsi
import firelib.indicators.SimpleMovingAverage

data class BolingerBands(val ma: Double, val lowerBand: Double, val upperBand: Double) {
    val arr = arrayOf(ma, lowerBand, upperBand)
}

object RsiBolingerSignals {

    fun makeRsiOptions(
        ann: List<HShape>,
        hours: List<Ohlc>,
        rsi: List<Double>,
        bolinger: List<BolingerBands>,
        title: String
    ): HOptions {
        val options = RenderUtils.makeOptions(hours, title, 70)
        options.annotations = listOf(HAnnotation(labels = emptyList(), shapes = ann))

        val rsiSeries = rsi.indices.map {
            arrayOf(hours[it].endTime.toEpochMilli().toDouble(), rsi[it])
        }

        repeat(3, { idx ->
            val maSeries = rsi.indices.map {
                arrayOf(hours[it].endTime.toEpochMilli().toDouble(), bolinger[it].arr[idx])
            }
            options.series += HSeries(
                "line",
                "ma-${idx}",
                data = maSeries,
                marker = HMarker(false),
                showInLegend = false
            )
        })

        fun addRsiThreshold(thresh: Double) {
            options.series += HSeries(
                "line", "upper", data = listOf(
                    arrayOf(hours.first().endTime.toEpochMilli().toDouble(), thresh),
                    arrayOf(hours.last().endTime.toEpochMilli().toDouble(), thresh)
                ), marker = HMarker(false), showInLegend = false, yAxis = 1, lineWidth = 0.5
            )
        }

        addRsiThreshold(25.0)
        addRsiThreshold(75.0)



        options.yAxis += HAxis(height = "30%", lineWidth = 1, offset = 10, opposite = false, top = "70%")
        options.series += HSeries(
            "column",
            "macd",
            data = emptyList(),
            marker = HMarker(true),
            showInLegend = false,
            yAxis = 1
        )
        options.series += HSeries(
            "line",
            "rsi",
            data = rsiSeries,
            marker = HMarker(false),
            showInLegend = true,
            yAxis = 1
        )

        return options
    }

    fun makeTitle(
        bolingerPeriod: Int,
        rsiPeriod: Int,
        rsiLow: Int,
        rsiHigh: Int,
        code: String,
        tf: String
    ): String {
        return "Rsi-Bolinger (bolinger=${bolingerPeriod}), (rsi = ${rsiPeriod}, ${rsiLow}, ${rsiHigh}) , ${code} ${tf}"
    }

    fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        window: Int,
        existing: Set<BreachEventKey>,
        settings: Map<String, String>
    ): BreachEvent? {
        val ohlcs = BotHelper.getOhlcsForTf(instr, tf.interval)

        val rsiPeriod = settings.getOrDefault(RsiBolingerCommand.RSI_ATTR, "14").toInt()
        val rsiHigh = settings.getOrDefault(RSI_HIGH_ATTR, "75").toInt()
        val rsiLow = settings.getOrDefault(RSI_LOW_ATTR, "25").toInt()
        val bolingerPeriod = settings.getOrDefault(RsiBolingerCommand.BOLINGER_ATTR, "20").toInt()

        val result = render(
            ohlcs, bolingerPeriod, rsiPeriod, rsiLow, rsiHigh,
            "Signal: ${makeTitle(bolingerPeriod, rsiPeriod, rsiLow, rsiHigh, instr.code, tf.name)}"
        )
        val suffix = "_${bolingerPeriod}_${rsiPeriod}_${rsiLow}_${rsiHigh}"

        if (result.signals.isNotEmpty()) {
            val last = result.signals.last()
            val time = ohlcs[last.first].endTime
            val key = BreachEventKey(instr.id + suffix, tf, time.toEpochMilli(), BreachType.MACD)
            val newSignal = last.first > ohlcs.size - window && !existing.contains(key)
            if (newSignal) {
                val img = ChartService.post(result.options)

                val fileName = makeSnapFileName(
                    BreachType.MACD.name,
                    instr.id + suffix,
                    tf,
                    time.toEpochMilli()
                )
                BotHelper.saveFile(img, fileName)
                return BreachEvent(key, fileName)
            }
        }
        return null
    }

    fun render(
        ohlcs: List<Ohlc>,
        bolingerPeriod: Int,
        rsiPeriod: Int,
        rsiLowThreshold: Int,
        rsiUpThreshold: Int,
        title: String
    ): MacdResult {

        val simpleMa = SimpleMovingAverage(bolingerPeriod, true)

        simpleMa.initMa(ohlcs.first().close)

        val bands = ohlcs.map {
            simpleMa.add(it.normalPrice())
            BolingerBands(
                simpleMa.value(),
                simpleMa.value() - 2 * simpleMa.sko(),
                simpleMa.value() + 2 * simpleMa.sko()
            )
        }

        val rsi = Rsi(rsiPeriod)
        val rsiSeries = ohlcs.map { rsi.addOhlc(it) }

        val signals = rsiSeries.flatMapIndexed { idx, value ->
            if (idx > rsiPeriod) {
                if (value > rsiUpThreshold && ohlcs[idx].high > bands[idx].upperBand) {
                    listOf(Pair(idx, Side.Sell))
                } else if (value < rsiLowThreshold && ohlcs[idx].low < bands[idx].lowerBand) {
                    listOf(Pair(idx, Side.Buy))
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
        val hShapes = MacdSignals.createAnnotations(signals, ohlcs)
        val options = makeRsiOptions(hShapes, ohlcs, rsiSeries, bands, title)
        return MacdResult(signals, options)
    }

}

fun main() {
    initDatabase()
    //val ticker = InstrId(code = "VTBR", market = "1", source = SourceName.FINAM.name)
    val ticker = InstrId(code = "KOS", market = "XNAS", source = SourceName.POLIGON.name)
    MdStorageImpl().updateMarketData(ticker, Interval.Min10);
    RsiBolingerSignals.checkSignals(ticker, TimeFrame.D, 600, emptySet(), emptyMap())
}
