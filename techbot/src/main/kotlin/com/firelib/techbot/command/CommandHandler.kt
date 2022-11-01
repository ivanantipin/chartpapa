package com.firelib.techbot.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User

interface CommandHandler {
    fun command(): String
    suspend fun handle(cmd: Cmd, bot: Bot, user : User)
}