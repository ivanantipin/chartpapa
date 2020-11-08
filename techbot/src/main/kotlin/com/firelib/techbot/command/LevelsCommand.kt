package com.firelib.techbot.command

import chart.HistoricalLevels
import com.firelib.techbot.BotHelper
import com.firelib.techbot.BotHelper.checkTicker
import com.firelib.techbot.initDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import picocli.CommandLine
import java.io.File


class LevelsCommand : CommandHandler {

    companion object {
        const val command = "/lvl"
    }

    override fun category() : CommandCategory{
        return CommandCategory.Analysis
    }



    @CommandLine.Command(name = command, description = ["display levels"])
    class LevelsCmd : CmdLine {
        @CommandLine.Parameters(description = ["ticker"])
        var ticker: String = ""

        override fun postConstruct() {
            ticker = ticker.toUpperCase()
        }
    }

    override fun command(): String {
        return command
    }

    override fun handle(cmd: Command, bot: Bot, update: Update) {

        val trendsCmd = LevelsCmd()

        if (!BotHelper.parseCommand(trendsCmd, cmd.opts, bot, update) ||
            !checkTicker(trendsCmd.ticker, bot, update)
        ) {
            return
        }

        val image = HistoricalLevels.historicalLevels(trendsCmd.ticker)
        bot.sendPhoto(chatId = update.message!!.chat.id, photo = File(image.filePath))
    }

    override fun description(): String {
        return "display levels for ticker"
    }

}

fun main() {
    initDatabase()
    HistoricalLevels.historicalLevels("AUDUSD")
}

