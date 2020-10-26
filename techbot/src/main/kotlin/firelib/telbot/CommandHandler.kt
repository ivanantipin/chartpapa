package com.github.kotlintelegrambot.echo.com.firelib.telbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

interface CommandHandler {
    fun commands() : List<String>
    suspend fun handle(cmd: Command, bot: Bot, update: Update)
    fun description() : String
}