package com.firelib.techbot

import com.firelib.techbot.domain.UserId
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile


class BotInterfaceImpl(val bot: Bot) : BotInterface {

    override suspend fun sendPhoto(img: ByteArray, users: List<UserId>, caption : String) {
        users.forEach { userId ->
            mainLogger.info("notifiying user ${userId}")
            val response = bot.sendPhoto(ChatId.fromId(userId.id), TelegramFile.ByByteArray(img), caption = caption)
            if (response.second != null) {
                response.second!!.printStackTrace()
            }
        }
    }

    override fun sendMessage(msg: String, users: List<UserId>) {
        users.forEach { userId ->
            val response = bot.sendMessage(ChatId.fromId(userId.id), msg)
            if (response.second != null) {
                response.second!!.printStackTrace()
            }
        }
    }
}