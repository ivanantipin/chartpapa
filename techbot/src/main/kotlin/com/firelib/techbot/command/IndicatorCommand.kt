package com.firelib.techbot.command

import chart.SignalType
import com.firelib.techbot.BotHelper
import com.firelib.techbot.TechBotApp
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.getId
import com.firelib.techbot.menu.chatId
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import java.io.File

class IndicatorCommand(val techBotApp: TechBotApp) {

    companion object {
        val indi = SignalType.values().associateBy({ it.settingsName }, { it.signalGenerator })
    }

    fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val userId = update.chatId().getId()
        val signalGenerator = indi[cmd.handlerName]!!
        val settings = signalGenerator.fetchSettings(userId)
        val instrId = cmd.instr(techBotApp.staticDataService())
        val tkr = instrId.code
        val tf = cmd.tf()
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instrId, tf.interval).value
        val hOptions = signalGenerator.drawPicture(instrId, tf, settings, techBotApp)
        val bytes = ChartService.post(hOptions)
        val fileName = BreachEvents.makeSnapFileName(
            cmd.handlerName,
            tkr,
            tf,
            ohlcs.last().endTime.toEpochMilli()
        )
        BotHelper.saveFile(bytes, fileName)
        bot.sendPhoto(chatId = update.chatId(), photo = File(fileName))
    }
}