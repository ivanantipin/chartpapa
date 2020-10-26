package com.github.kotlintelegrambot.echo.com.firelib.telbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.echo.chart.ImageService
import com.github.kotlintelegrambot.entities.Update
import firelib.telbot.BotHelper
import firelib.telbot.BotHelper.checkTicker
import firelib.telbot.TimeFrame
import picocli.CommandLine
import java.io.File


class TrendsCommand : CommandHandler {

    companion object {
        const val command = "/lines"
    }


    @CommandLine.Command(name = command, description = ["display trends for commands"])
    class TrendsCmd : CmdLine{
        @CommandLine.Parameters(description = ["ticker"])
        var ticker: String = ""

        @CommandLine.Parameters(description = ["timeframe , possible values: \${COMPLETION-CANDIDATES}"], defaultValue = "H")
        var tf: TimeFrame = TimeFrame.D;
        override fun postConstruct() {
            ticker = ticker.toLowerCase()
        }
    }

    override fun commands(): List<String> {
        return listOf(command)
    }

    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {

        val trendsCmd = TrendsCmd()

        if (!BotHelper.parseCommand(trendsCmd, cmd.opts, bot, update) ||
            !checkTicker(trendsCmd.ticker, bot, update)
        ) {
            return
        }

        val image = ImageService.historicalBreaches(trendsCmd.ticker, trendsCmd.tf)
        bot.sendPhoto(chatId = update.message!!.chat.id, photo = File(image.filePath))
    }

    override fun description(): String {
        return "display all trend lines"
    }

}

