package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.getId
import com.firelib.techbot.macd.RsiBolingerSignals
import com.firelib.techbot.mainLogger
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.persistence.Settings
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.core.misc.JsonHelper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File


class RsiBolingerCommand : CommandHandler {

    companion object{
        val name = "rbc"

        const val BOLINGER_ATTR = "bolinger"
        const val RSI_ATTR = "rsi"
        const val RSI_LOW_ATTR = "rsiLow"
        const val RSI_HIGH_ATTR = "rsiHigh"

        fun validate(split: List<String>) : Boolean{
            try {
                if(split.size != 6){
                    return false
                }
                split.subList(2, split.size).forEach {
                    it.toInt()
                }
                require(split[4].toInt() < split[5].toInt())
                split.subList(2, split.size).forEach { it.toInt() }
            }catch (e : Exception){
                mainLogger.error("can not set rbc settings with command ${split}")
                return false
            }
            return true
        }

        fun parsePayload(split : List<String>) : Map<String,String>{
            return mapOf(
                "command" to split[1],
                BOLINGER_ATTR to split[2],
                RSI_ATTR to split[3],
                RSI_LOW_ATTR to split[4],
                RSI_HIGH_ATTR to split[5],
            )
        }
        fun displayHelp(bot: Bot, update: Update) {
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
            Settings.select { (Settings.user eq userId) and (Settings.name eq name) }.map { it[Settings.value] }.firstOrNull()
        }

        var bolingerPeriod = 20
        var rsiPeriod = 14
        var rsiLow = 25
        var rsiHigh = 75


        if(value != null){
            val settings = JsonHelper.fromJson<Map<String, String>>(value)
            bolingerPeriod = settings[BOLINGER_ATTR]!!.toInt()
            rsiPeriod = settings[RSI_ATTR]!!.toInt()
            rsiLow = settings[RSI_LOW_ATTR]!!.toInt()
            rsiHigh = settings[RSI_HIGH_ATTR]!!.toInt()
        }

        val instrId = cmd.instr()
        val tkr = instrId.code
        val tf = cmd.tf()

        val ohlcs = BotHelper.getOhlcsForTf(instrId, tf.interval)

        val title = RsiBolingerSignals.makeTitle(bolingerPeriod, rsiPeriod, rsiLow, rsiHigh, instrId.code, tf.name)

        val macdResult = RsiBolingerSignals.render(ohlcs, bolingerPeriod, rsiPeriod, rsiLow,rsiHigh, title)

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