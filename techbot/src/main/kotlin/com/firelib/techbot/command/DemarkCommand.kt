package com.firelib.techbot.command

import chart.BreachFinder
import com.firelib.techbot.BotHelper
import com.firelib.techbot.BotHelper.checkTicker
import com.firelib.techbot.chart.SequentaAnnCreator
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.saveFile
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import picocli.CommandLine
import java.io.File


class DemarkCommand : CommandHandler {

    companion object {
        const val command = "/demark"
    }


    @CommandLine.Command(name = command, description = ["demark sequenta indicator"])
    class DemarkCmd : CmdLine{
        @CommandLine.Parameters(description = ["ticker"])
        var ticker: String = ""

        @CommandLine.Parameters(description = ["timeframe , possible values: \${COMPLETION-CANDIDATES}"], defaultValue = "H")
        var timeFrame: TimeFrame = TimeFrame.D;
        override fun postConstruct() {
            ticker = ticker.toUpperCase()
        }
    }

    override fun commands(): List<String> {
        return listOf(command)
    }

    override fun handle(cmd: Command, bot: Bot, update: Update) {

        val dcmd = DemarkCmd()

        if (!BotHelper.parseCommand(dcmd, cmd.opts, bot, update) ||
            !checkTicker(dcmd.ticker, bot, update)
        ) {
            return
        }

        val ohlcs = BotHelper.getOhlcsForTf(dcmd.ticker, dcmd.timeFrame.interval)

        val ann = SequentaAnnCreator.createAnnotations(ohlcs)

        val bytes = ChartService.drawSequenta(ann, ohlcs, "Demark indicator for ${dcmd.ticker} (${dcmd.timeFrame})" )

        val fileName = BreachFinder.makeSnapFileName(
            "demark",
            dcmd.ticker,
            dcmd.timeFrame,
            ohlcs.last().endTime.toEpochMilli()
        )
        saveFile(bytes, fileName)

        bot.sendPhoto(chatId = update.message!!.chat.id, photo = File(fileName))
    }

    override fun description(): String {
        return "display demark sequenta"
    }

}

