package com.firelib.techbot.command

import com.github.kotlintelegrambot.Bot
import com.firelib.techbot.Cmd
import com.github.kotlintelegrambot.entities.Update

interface CommandHandler {
    fun command(): String
    fun handle(cmd: Cmd, bot: Bot, update: Update)
}