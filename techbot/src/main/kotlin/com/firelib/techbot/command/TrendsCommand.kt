package com.firelib.techbot.command

import chart.HistoricalTrendLines.historicalTrendLines
import com.github.kotlintelegrambot.Bot
import com.firelib.techbot.menu.chatId
import com.github.kotlintelegrambot.entities.Update
import java.io.File


class TrendsCommand : CommandHandler {

    companion object{
        val name = "tl"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val image = historicalTrendLines(cmd.instr(), cmd.tf())
        bot.sendPhoto(chatId = update.chatId(), photo = File(image.filePath))
    }

}

