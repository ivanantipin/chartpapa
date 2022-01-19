package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.Debt2FCFCharter
import com.firelib.techbot.chart.RenderUtils
import com.firelib.techbot.getId
import com.firelib.techbot.initDatabase
import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.persistence.Settings
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.InstrId
import firelib.core.misc.JsonHelper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

// macd 12 26 9 sber 1d
class MacdCommand : CommandHandler {

    companion object{
        val name = "macd"

        fun validate(split: List<String>) : Boolean{
            try {
                if(split.size != 5){
                    return false
                }
                split.subList(2, split.size).forEach { it.toInt() }
            }catch (e : Exception){
                return false
            }
            return true
        }

        fun parsePayload(split : List<String>) : Map<String,String>{
            return mapOf(
                "command" to split[0],
                "shortEma" to split[1],
                "longEma" to split[2],
                "signalEma" to split[3],
            )
        }
        fun displayMACD_Help(bot: Bot, update: Update) {
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




    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {

        val userId = update.chatId().getId().toInt()

        val value = transaction {
            Settings.select { (Settings.user eq userId) and (Settings.name eq "macd") }.map { it[Settings.value] }.firstOrNull()
        }

        var shortEma = 12//cmd.opts["short.ema"]!!.toInt()
        var longEma = 26//cmd.opts["long.ema"]!!.toInt()
        var signalEma = 9//cmd.opts["signal.ema"]!!.toInt()

        if(value != null){
            val settings = JsonHelper.fromJson<Map<String, String>>(value)
            shortEma = settings["shortEma"]!!.toInt()
            longEma = settings["longEma"]!!.toInt()
            signalEma = settings["signalEma"]!!.toInt()
        }

        val instrId = cmd.instr()
        val tkr = instrId.code
        val tf = cmd.tf()

        val ohlcs = BotHelper.getOhlcsForTf(instrId, tf.interval)

        val title = "Macd(${shortEma},${longEma},${signalEma}) ${instrId.code} (${tf.name})"

        val macdResult = MacdSignals.render(ohlcs, shortEma, longEma, signalEma, title)

        val bytes = ChartService.post(macdResult.options)

        val fileName = BreachEvents.makeSnapFileName(
            name,
            tkr,
            tf,
            ohlcs.last().endTime.toEpochMilli()
        )
        BotHelper.saveFile(bytes, fileName)

        bot.sendPhoto(chatId = update.chatId(), photo = File(fileName))
    }
}

fun main() {
    initDatabase()
    //println(EodHistSource().symbols().size)
    //return
    val byInstrId = FundamentalService.debtToFcF(InstrId.dummyInstrument("vet"))
    ChartService.post(
        Debt2FCFCharter.makeSeries(byInstrId[0], byInstrId[1], "FCF to Debt"),
        RenderUtils.GLOBAL_OPTIONS_FOR_BILLIONS,
        "Chart"
    )
}