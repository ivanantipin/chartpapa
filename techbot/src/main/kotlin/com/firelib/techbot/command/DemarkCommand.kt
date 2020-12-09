package com.firelib.techbot.command

import chart.BreachFinder
import com.firelib.techbot.BotHelper
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.SequentaAnnCreator
import com.firelib.techbot.saveFile
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Cmd
import com.github.kotlintelegrambot.dispatcher.chatId
import com.github.kotlintelegrambot.entities.Update
import java.io.File


class DemarkCommand : CommandHandler {

    override fun command(): String {
        return "dema"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {

        val (tkr, tf) = cmd.tickerAndTf()

        val ohlcs = BotHelper.getOhlcsForTf(tkr, tf.interval)

        val ann = SequentaAnnCreator.createAnnotations(ohlcs)

        val bytes = ChartService.drawSequenta(ann, ohlcs, "Demark indicator for ${tkr} (${tf})")

        val fileName = BreachFinder.makeSnapFileName(
            "demark",
            tkr,
            tf,
            ohlcs.last().endTime.toEpochMilli()
        )
        saveFile(bytes, fileName)

        bot.sendPhoto(chatId = update.chatId(), photo = File(fileName))
    }
}