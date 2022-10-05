package com.firelib.techbot

import com.firelib.techbot.breachevent.BreachEvent
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import java.io.File

interface BotInterface {
    fun sendBreachEvent(be: BreachEvent, users: List<UserId>)
}

class BotInterfaceImpl(val bot: Bot) : BotInterface{
    override fun sendBreachEvent(be: BreachEvent, users: List<UserId>) {
        users.forEach { userId ->
            mainLogger.info("notifiying user ${userId}")
            val response = bot.sendPhoto(ChatId.fromId(userId.id), File(be.photoFile))
            if (response.second != null) {
                response.second!!.printStackTrace()
            }
        }
    }
}