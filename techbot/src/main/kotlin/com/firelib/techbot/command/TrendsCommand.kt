package com.firelib.techbot.command

import chart.HistoricalTrendLines.historicalTrendLines
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Cmd
import com.github.kotlintelegrambot.dispatcher.chatId
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.InstrId
import java.io.File


class TrendsCommand : CommandHandler {

    override fun command(): String {
        return "tl"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val (tkr, market, tf) = cmd.tickerAndTf()
        val image = historicalTrendLines(InstrId(tkr, market), tf)
        bot.sendPhoto(chatId = update.chatId(), photo = File(image.filePath))
    }

}

