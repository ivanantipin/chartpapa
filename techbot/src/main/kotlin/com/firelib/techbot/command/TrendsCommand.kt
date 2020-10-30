package com.firelib.techbot.command

import chart.BreachFinder.historicalBreaches
import com.firelib.techbot.BotHelper
import com.firelib.techbot.BotHelper.checkTicker
import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import picocli.CommandLine
import java.io.File


class TrendsCommand : CommandHandler {

    companion object {
        const val command = "/tl"
    }


    @CommandLine.Command(name = command, description = ["display trends for commands"])
    class TrendsCmd : CmdLine{
        @CommandLine.Parameters(description = ["ticker"])
        var ticker: String = ""

        @CommandLine.Parameters(description = ["timeframe , possible values: \${COMPLETION-CANDIDATES}"], defaultValue = "D")
        var tf: TimeFrame = TimeFrame.D;
        override fun postConstruct() {
            ticker = ticker.toUpperCase()
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

        val image = historicalBreaches(trendsCmd.ticker, trendsCmd.tf)
        bot.sendPhoto(chatId = update.message!!.chat.id, photo = File(image.filePath))
    }

    override fun description(): String {
        return "display trend lines for ticker"
    }

}

