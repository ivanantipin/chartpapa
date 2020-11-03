package com.firelib.techbot.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

interface CommandHandler {
    fun commands() : List<String>
    fun handle(cmd: Command, bot: Bot, update: Update)
    fun description() : String
}