package com.firelib.techbot.macd

import chart.BreachType
import chart.SignalType
import com.firelib.techbot.BotHelper
import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.TechBotApp
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents.makeSnapFileName
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.RenderUtils
import com.firelib.techbot.chart.domain.*
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.menu.chatId
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.indicators.EmaSimple

object MacdSignals : SignalGenerator {

    fun createAnnotations(signals: List<Pair<Int, Side>>, ohlcs: List<Ohlc>): List<HShape> {
        val shapes = ArrayList<HShape>()

        signals.forEach { (ci, s) ->
            val oh = ohlcs[ci]
            val up = s.sign < 0
            val level = if (up) oh.high else oh.low
            val point = HPoint(x = oh.endTime.toEpochMilli(), y = level, xAxis = 0, yAxis = 0)
            if (up) {
                shapes.add(RenderUtils.makeBuySellPoint("red", point.x!!, point.y!!, Side.Sell))
            } else {
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

    fun addChart(options: HOptions, data: List<Array<Double>>, signal: List<Array<Double>>) {
        options.yAxis += HAxis(height = "30%", lineWidth = 1, offset = 10, opposite = false, top = "70%")
        options.series += HSeries("column", "macd", data = data, marker = HMarker(true), showInLegend = true, yAxis = 1)
        options.series += HSeries(
            "line",
            "signal",
            data = signal,
            marker = HMarker(false),
            showInLegend = true,
            yAxis = 1
        )
    }

    override fun signalType(): SignalType {
        return SignalType.MACD
    }

    override fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        window: Int,
        existing: Set<BreachEventKey>,
        settings: Map<String, String>,
        techBotApp: TechBotApp
    ): List<BreachEvent> {
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval).value

        if (ohlcs.isEmpty()) {
            return emptyList()
        }

        val macdParams = MacdParams.fromSettings(settings)
        val result = render(
            ohlcs,
            macdParams,
            "Signal: ${makeTitle(tf, instr, settings)}"
        )
        val suffix = "_${macdParams.shortEma}_${macdParams.longEma}_${macdParams.signalEma}"

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
                return listOf(BreachEvent(key, fileName))
            }
        }
        return emptyList()
    }

    override fun drawPicture(
        instr: InstrId,
        tf: TimeFrame,
        settings: Map<String, String>,
        techBotApp: TechBotApp
    ): HOptions {
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval).value
        val macdParams = MacdParams.fromSettings(settings)
        return render(ohlcs, macdParams, makeTitle(tf, instr, settings)).options
    }

    fun makeTitle(timeFrame: TimeFrame, instr: InstrId, settings: Map<String, String>): String {
        val params = MacdParams.fromSettings(settings)
        return "Macd(${params.shortEma},${params.longEma},${params.signalEma}) ${instr.code} ${timeFrame.name}"
    }

    fun render(ohlcs: List<Ohlc>, macdParams: MacdParams, title: String): MacdResult {

        val shortEma = EmaSimple(macdParams.shortEma, ohlcs.first().close)
        val longEma = EmaSimple(macdParams.longEma, ohlcs.first().close)
        val signalEma = EmaSimple(macdParams.signalEma, 0.0)

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
            if (idx > 26) {
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

        val options = makeMacdOptions(hShapes, ohlcs, macd, signal, title)
        return MacdResult(signals, options)
    }

    override fun validate(split: List<String>): Boolean {
        try {
            if (split.size != 5) {
                return false
            }
            split.subList(2, split.size).forEach { it.toInt() }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun parsePayload(split: List<String>): Map<String, String> {
        return mapOf(
            "command" to split[1],
            "shortEma" to split[2],
            "longEma" to split[3],
            "signalEma" to split[4],
        )
    }

    override fun displayHelp(bot: Bot, update: Update) {
        //fixme internationalize
        val header = """
        *Конфигурация индикатора MACD*
        
        Вы можете установить параметры вашего макд с помощью команды:
                
        ``` /set macd <shortEma> <longEma> <signalEma>```
                
        *пример*
        
        ``` /set macd 12 26 9```
        
        по умолчанию параметры 
        shortEma=12 
        longEma=26 
        signalEma=9
        
        более подробно читайте об индикаторе например "[здесь](https://ru.tradingview.com/chart/BTCUSD/LD80HDLn-indikator-macd-printsip-raboty-sekrety-nahozhdeniya-divergentsij)"
                      
    """.trimIndent()


        bot.sendMessage(
            chatId = update.chatId(),
            text = header,
            parseMode = ParseMode.MARKDOWN
        )
    }
}

data class MacdResult(
    val signals: List<Pair<Int, Side>>,
    val options: HOptions
)

data class MacdParams(
    val longEma: Int,
    val shortEma: Int,
    val signalEma: Int
) {
    companion object {
        fun fromSettings(settings: Map<String, String>): MacdParams {
            val longEma = settings.getOrDefault("longEma", "26").toInt()
            val shortEma = settings.getOrDefault("shortEma", "12").toInt()
            val signalEma = settings.getOrDefault("signalEma", "9").toInt()
            return MacdParams(longEma, shortEma, signalEma)
        }
    }
}
