package com.firelib.techbot.macd

import chart.BreachType
import com.firelib.techbot.*
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents.makeSnapFileName
import com.firelib.techbot.chart.RenderUtils
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.domain.*
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.sequenta.SequentaAnnCreator
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.core.store.MdStorageImpl
import firelib.indicators.EmaSimple
import org.jetbrains.exposed.sql.logTimeSpent

data class MacdResult(
    val signals : List<Pair<Int, Side>>,
    val options : HOptions
)

object MacdSignals {

    fun createAnnotations(signals: List<Pair<Int, Side>>, ohlcs: List<Ohlc>): List<HShape> {
        val shapes = ArrayList<HShape>()

        signals.forEach { (ci, s) ->
            val oh = ohlcs[ci]
            val up = s.sign < 0
            val level = if (up) oh.high else oh.low
            val point = HPoint(x = oh.endTime.toEpochMilli(), y = level, xAxis = 0, yAxis = 0)
            if(up){
                shapes.add(RenderUtils.makeBuySellPoint("red", point.x!!, point.y!!, Side.Sell))
            }else{
                shapes.add(RenderUtils.makeBuySellPoint("green", point.x!!, point.y!!, Side.Buy))
            }
        }
        return shapes
    }

    fun makeMacdOptions(
        ann: List<HShape>,
        hours: List<Ohlc>,
        macd: List<Double>,
        signal: List<Double>,
        title: String
    ): HOptions {
        val options = RenderUtils.makeOptions(hours, title)
        options.annotations = listOf(HAnnotation(labels = emptyList(), shapes = ann))

        val macdSeries = macd.indices.map {
            arrayOf(hours[it].endTime.toEpochMilli().toDouble(), macd[it])
        }
        val signalSeries = macd.indices.map {
            arrayOf(hours[it].endTime.toEpochMilli().toDouble(), signal[it])
        }

        addChart(options, macdSeries, signalSeries)

        return options
    }

    fun addChart(options: HOptions, data : List<Array<Double>>, signal : List<Array<Double>>,){
        options.yAxis += HAxis(height = "30%", lineWidth = 1, offset = 10, opposite = false, top = "70%")
        options.series += HSeries("column", "macd", data = data, marker = HMarker(true), showInLegend = true, yAxis = 1)
        options.series += HSeries("line", "signal", data = signal, marker = HMarker(false), showInLegend = true, yAxis = 1)
    }

    fun checkSignals(instr: InstrId, tf: TimeFrame, window: Int, existing: Set<BreachEventKey>): BreachEvent? {
        val ohlcs = BotHelper.getOhlcsForTf(instr, tf.interval)

        val result = render(ohlcs, 12, 26, 9)

        if(result.signals.isNotEmpty()){
            val last = result.signals.last()
            val time = ohlcs[last.first].endTime
            val key = BreachEventKey(instr.id, tf, time.toEpochMilli(), BreachType.DEMARK_SIGNAL)
            val newSignal = last.first > ohlcs.size - window && !existing.contains(key)
            if (newSignal) {
                val img = ChartService.post(result.options)
                val fileName = makeSnapFileName(
                    BreachType.MACD.name,
                    instr.id,
                    tf,
                    time.toEpochMilli()
                )
                BotHelper.saveFile(img, fileName)
                return BreachEvent(key, fileName)
            }
        }
        return null
    }

    fun render(ohlcs: List<Ohlc>, shortEmaLen : Int, longEmaLen : Int, signalEmaLen : Int): MacdResult {
        val shortEma = EmaSimple(shortEmaLen, ohlcs.first().close)
        val longEma = EmaSimple(longEmaLen, ohlcs.first().close)
        val signalEma = EmaSimple(signalEmaLen, 0.0)

        val macd = ohlcs.map {
            longEma.onRoll(it.close)
            shortEma.onRoll(it.close)
            shortEma.value() - longEma.value()
        }
        val signal = macd.map {
            signalEma.onRoll(it)
            signalEma.value()
        }

        val signals = signal.flatMapIndexed { idx, value ->
            if (idx > 13) {
                val currValue = macd[idx] - signal[idx]
                val prevValue = macd[idx - 1] - signal[idx - 1]
                if (currValue * prevValue < 0) {
                    listOf(Pair(idx, if (currValue > 0) Side.Buy else Side.Sell))
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

        val hShapes = createAnnotations(signals, ohlcs)

        val options = makeMacdOptions(hShapes, ohlcs, macd, signal, "macd")
        return MacdResult(signals, options)
    }

}

fun main() {
    initDatabase()
    val ticker = InstrId(code = "GAZP", market = "1", source = SourceName.FINAM.name)
    //val ticker = InstrId(code = "VIST", market = "XNAS", source = SourceName.POLIGON.name)
    MdStorageImpl().updateMarketData(ticker, Interval.Min10);
    MacdSignals.checkSignals(ticker, TimeFrame.D, 180, emptySet())
}
