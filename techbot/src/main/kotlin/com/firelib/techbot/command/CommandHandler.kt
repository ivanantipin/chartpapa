package com.firelib.techbot.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update


enum class CommandCategory{
    Analysis, Subscriptions, Other
}

interface CommandHandler {
    fun command() : String
    fun handle(cmd: Command, bot: Bot, update: Update)
    fun description() : String

    fun category() : CommandCategory{
        return CommandCategory.Other
    }
}