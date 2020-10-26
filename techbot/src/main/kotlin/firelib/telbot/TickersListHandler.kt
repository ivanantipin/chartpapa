package com.github.kotlintelegrambot.echo.firelib.telbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.echo.com.firelib.telbot.Command
import com.github.kotlintelegrambot.echo.com.firelib.telbot.CommandHandler
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.model.tickers

class TickersListHandler : CommandHandler {
    override fun commands(): List<String> {
        return listOf("list")
    }

    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {
        val out = tickers.chunked(3).map { it.joinToString(separator = ",") }.joinToString("\n")

        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = out,
            parseMode = ParseMode.MARKDOWN
        )
    }

    override fun description(): String {
        return "display tickers list"
    }
}