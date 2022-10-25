package com.firelib.techbot

import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.domain.UserId
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile

class BotInterfaceImpl(val bot: Bot) : BotInterface {

    override fun sendBreachEvent(img: ByteArray, users: List<UserId>) {
        users.forEach { userId ->
            mainLogger.info("notifiying user ${userId}")
            val response = bot.sendPhoto(ChatId.fromId(userId.id), TelegramFile.ByByteArray(img))
            if (response.second != null) {
                response.second!!.printStackTrace()
            }
        }
    }
}