package com.github.kotlintelegrambot.echo.firelib.telbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.echo.com.firelib.telbot.Command
import com.github.kotlintelegrambot.echo.com.firelib.telbot.CommandHandler
import com.github.kotlintelegrambot.echo.com.firelib.telbot.TABot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update

class UnknownCmdHandler(val taBot: TABot) : CommandHandler {
    override fun commands(): List<String> {
        return listOf("")
    }

    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {

        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = "Unknown command, available commands:",
            parseMode = ParseMode.MARKDOWN
        )
        HelpListHandler(taBot).handle(cmd, bot, update)
    }

    override fun description(): String {
        return "display trend lines"
    }
}