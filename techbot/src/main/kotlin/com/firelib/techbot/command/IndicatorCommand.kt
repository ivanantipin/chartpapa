package com.firelib.techbot.command

import com.firelib.techbot.SignalType
import com.firelib.techbot.TechbotApp
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.getId
import com.firelib.techbot.menu.chatId
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.User
import firelib.core.domain.InstrId

class IndicatorCommand(val techBotApp: TechbotApp) {

    companion object {
        val indi = SignalType.values().associateBy({ it.name }, { it.signalGenerator })
    }

    suspend fun handle(cmd: Cmd, bot: Bot, update: User) {
        val userId = update.chatId().getId()
        val signalGenerator = indi[cmd.handlerName]!!
        val settings = signalGenerator.fetchSettings(userId)
        val code = cmd.instrId()
        val instruments = if(code == "All"){
            techBotApp.subscriptionService().subscriptions[UserId(
                update.chatId().getId()
            )]!!.values.distinct()
        }else{
            listOf(techBotApp.instrumentsService().byId(code))

        }

        val tfs = cmd.tf()

        tfs.forEach { tf->
            instruments.forEach {instrId->
                val ohlcs = techBotApp.ohlcService().getOhlcsForTf(instrId, tf.interval)
                val hOptions = signalGenerator.drawPicture(instrId, tf, settings, ohlcs)
                val bytes = ChartService.post(hOptions)
                bot.sendPhoto(chatId = update.chatId(), TelegramFile.ByByteArray(bytes), "#${instrId.code}")
            }
        }

    }
}