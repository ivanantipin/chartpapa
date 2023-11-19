package com.firelib.techbot.command

import com.firelib.techbot.SignalType
import com.firelib.techbot.TechbotApp
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.getId
import com.firelib.techbot.menu.chatId
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User

class IndicatorCommand(val techBotApp: TechbotApp) {

    companion object {
        val indi = SignalType.values().associateBy({ it.name }, { it.signalGenerator })
    }

    suspend fun handle(cmd: Cmd, bot: Bot, update: User) {
        val userId = update.chatId().getId()
        val signalGenerator = indi[cmd.handlerName]!!
        val settings = signalGenerator.fetchSettings(userId)
        val instrId = cmd.instr(techBotApp.instrumentsService())
        val tf = cmd.tf()
        val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instrId, tf.interval)
        val hOptions = signalGenerator.drawPicture(instrId, tf, settings, ohlcs)
        val bytes = ChartService.post(hOptions)
        bot.sendMessage(chatId = update.chatId(), "#${hOptions.title}")
        bot.sendPhoto(chatId = update.chatId(), TelegramFile.ByByteArray(bytes))
    }
}