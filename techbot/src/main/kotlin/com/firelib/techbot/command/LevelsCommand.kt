package com.firelib.techbot.command

import chart.HistoricalLevels
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Cmd
import com.github.kotlintelegrambot.dispatcher.chatId
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.InstrId
import java.io.File


class LevelsCommand : CommandHandler {

    override fun command(): String {
        return "lvl"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val (tkr, market, tf) = cmd.tickerAndTf()
        val image = HistoricalLevels.historicalLevels(InstrId(tkr, market))
        bot.sendPhoto(chatId = update.chatId(), photo = File(image.filePath))
    }
}
