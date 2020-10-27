package com.github.kotlintelegrambot.echo.firelib.telbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.echo.com.firelib.telbot.Command
import com.github.kotlintelegrambot.echo.com.firelib.telbot.CommandHandler
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.finam.FinamDownloader
import firelib.telbot.SymbolsDao

class TickersListHandler : CommandHandler {
    override fun commands(): List<String> {
        return listOf("list")
    }

    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {
        val byMarket = SymbolsDao.available().groupBy { it.market }

        var str = ""

        val headers = mapOf(
            FinamDownloader.FX to "Forex",
            FinamDownloader.SHARES_MARKET to "Stock"
        )

        byMarket.forEach {
            str += "\n*${headers[it.key]}*\n"
            str +=  it.value.map{it.code}.chunked(3).map { it.joinToString(separator = ",") }.joinToString("\n")
        }

        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = str,
            parseMode = ParseMode.MARKDOWN
        )
    }

    override fun description(): String {
        return "display tickers list"
    }
}