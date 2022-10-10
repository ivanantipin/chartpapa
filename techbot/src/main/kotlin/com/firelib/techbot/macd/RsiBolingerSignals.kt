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
import com.firelib.techbot.mainLogger
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.langCode
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import firelib.core.domain.Side
import firelib.indicators.Rsi
import firelib.indicators.SimpleMovingAverage

data class BolingerBands(val ma: Double, val lowerBand: Double, val upperBand: Double) {
    val arr = arrayOf(ma, lowerBand, upperBand)
}

val BOLINGER_ATTR = "bolinger"
val RSI_ATTR = "rsi"
val RSI_LOW_ATTR = "rsiLow"
val RSI_HIGH_ATTR = "rsiHigh"

data class RsiBolingerParams(
    val rsiPeriod: Int,
    val rsiHigh: Int,
    val rsiLow: Int,
    val bolingerPeriod: Int
) {
    companion object {
        fun fromSettings(settings: Map<String, String>): RsiBolingerParams {
            val rsiPeriod = settings.getOrDefault(RSI_ATTR, "14").toInt()
            val rsiHigh = settings.getOrDefault(RSI_HIGH_ATTR, "75").toInt()
            val rsiLow = settings.getOrDefault(RSI_LOW_ATTR, "25").toInt()
            val bolingerPeriod = settings.getOrDefault(BOLINGER_ATTR, "20").toInt()
            return RsiBolingerParams(rsiPeriod, rsiHigh, rsiLow, bolingerPeriod)
        }
    }
}

object RsiBolingerSignals : SignalGenerator {

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

    override fun signalType(): SignalType {
        return SignalType.RSI_BOLINGER
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
        val params = RsiBolingerParams.fromSettings(settings)

        val result = render(
            ohlcs, params, "Signal: ${makeTitle(tf, instr, settings)}"
        )
        val suffix = "_${params.bolingerPeriod}_${params.rsiPeriod}_${params.rsiLow}_${params.rsiHigh}"

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
        tf: TimeFrame, settings: Map<String, String>, techBotApp: TechBotApp
    ): HOptions {
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instr, tf.interval).value
        return render(ohlcs, RsiBolingerParams.fromSettings(settings), makeTitle(tf, instr, settings)).options
    }

    fun makeTitle(timeFrame: TimeFrame, instr: InstrId, settings: Map<String, String>): String {
        val params = RsiBolingerParams.fromSettings(settings)
        return makeTitle(
            params.bolingerPeriod,
            params.rsiPeriod,
            params.rsiLow,
            params.rsiHigh,
            instr.code,
            timeFrame.name
        )
    }

    fun render(
        ohlcs: List<Ohlc>,
        params: RsiBolingerParams,
        title: String
    ): MacdResult {

        val simpleMa = SimpleMovingAverage(params.bolingerPeriod, true)
        if (ohlcs.isEmpty()) {
            return MacdResult(emptyList(), HOptions())
        }

        simpleMa.initMa(ohlcs.first().close)

        val bands = ohlcs.map {
            simpleMa.add(it.normalPrice())
            BolingerBands(
                simpleMa.value(),
                simpleMa.value() - 2 * simpleMa.sko(),
                simpleMa.value() + 2 * simpleMa.sko()
            )
        }

        val rsi = Rsi(params.rsiPeriod)
        val rsiSeries = ohlcs.map { rsi.addOhlc(it) }

        val signals = rsiSeries.flatMapIndexed { idx, value ->
            if (idx > params.rsiPeriod) {
                if (value > params.rsiHigh && ohlcs[idx].high > bands[idx].upperBand) {
                    listOf(Pair(idx, Side.Sell))
                } else if (value < params.rsiLow && ohlcs[idx].low < bands[idx].lowerBand) {
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

    override fun validate(split: List<String>): Boolean {
        try {
            if (split.size != 6) {
                return false
            }
            split.subList(2, split.size).forEach {
                it.toInt()
            }
            require(split[4].toInt() < split[5].toInt())
            split.subList(2, split.size).forEach { it.toInt() }
        } catch (e: Exception) {
            mainLogger.error("can not set rbc settings with command ${split}")
            return false
        }
        return true
    }

    override fun parsePayload(split: List<String>): Map<String, String> {
        return mapOf(
            "command" to split[1],
            BOLINGER_ATTR to split[2],
            RSI_ATTR to split[3],
            RSI_LOW_ATTR to split[4],
            RSI_HIGH_ATTR to split[5],
        )
    }

    override fun displayHelp(bot: Bot, update: Update) {
        //fixme internationalize
        val header = """
        *Конфигурация индикатора RSI-BOLINGER*
        
        Вы можете установить параметры с помощью команды:
                
        ``` /set rbc <bolinger> <rsi> <rsiLow> <rsiHigh>```
                
        *пример*
        
        ``` /set rbc 20 14 25 75```
        
        по умолчанию параметры 
        bolinger=20 
        rsi=14 
        rsiLow=25
        rsiHigh=75                       
    """.trimIndent()
        update.langCode()
        bot.sendMessage(
            chatId = update.chatId(),
            text = header,
            parseMode = ParseMode.MARKDOWN
        )
    }
}