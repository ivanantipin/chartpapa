package com.github.kotlintelegrambot.echo.firelib.telbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.echo.com.firelib.telbot.Command
import com.github.kotlintelegrambot.echo.com.firelib.telbot.CommandHandler
import com.github.kotlintelegrambot.echo.com.firelib.telbot.TABot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update

class HelpListHandler(val taBot: TABot) : CommandHandler {

    override fun commands(): List<String> {
        return listOf("help")
    }

    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {

        val msg = taBot.map.values.map { "${it.commands().joinToString("|")} - ${it.description()}" }
            .joinToString(separator = "\n")

        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = msg,
            parseMode = ParseMode.MARKDOWN
        )
    }

    override fun description(): String {
        return "display help"
    }
}