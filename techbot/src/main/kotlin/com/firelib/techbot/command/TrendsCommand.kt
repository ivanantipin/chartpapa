package com.firelib.techbot.command

import chart.HistoricalTrendLines.historicalTrendLines
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Cmd
import com.github.kotlintelegrambot.dispatcher.chatId
import com.github.kotlintelegrambot.entities.Update
import firelib.core.SourceName
import firelib.core.domain.InstrId
import java.io.File


class TrendsCommand : CommandHandler {

    override fun command(): String {
        return "tl"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val image = historicalTrendLines(cmd.instr(), cmd.tf())
        bot.sendPhoto(chatId = update.chatId(), photo = File(image.filePath))
    }

}

