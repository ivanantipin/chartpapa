package com.firelib.techbot.command

import chart.BreachFinder
import com.firelib.techbot.BotHelper
import com.firelib.techbot.saveFile
import com.github.kotlintelegrambot.Bot
import com.firelib.techbot.Cmd
import com.firelib.techbot.chart.*
import com.firelib.techbot.initDatabase
import com.github.kotlintelegrambot.dispatcher.chatId
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.InstrId
import java.io.File


class DemarkCommand : CommandHandler {

    override fun command(): String {
        return "dema"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {

        val instrId = cmd.instr()
        val tkr = instrId.code
        val tf = cmd.tf()

        val ohlcs = BotHelper.getOhlcsForTf(instrId, tf.interval)

        val ann = SequentaAnnCreator.createAnnotations(ohlcs)

        val bytes = ChartService.drawSequenta(ann, ohlcs, "Demark indicator for ${tkr} (${tf})")

        val fileName = BreachFinder.makeSnapFileName(
            "demark",
            tkr,
            tf,
            ohlcs.last().endTime.toEpochMilli()
        )
        saveFile(bytes, fileName)

        bot.sendPhoto(chatId = update.chatId(),  photo = File(fileName))
    }
}

fun main() {
    initDatabase()
    //println(EodHistSource().symbols().size)
    //return
    val byInstrId = FundamentalService.debtToFcF(InstrId.dummyInstrument("vet"))
    ChartService.post(
        Debt2FCFCharter.makeSeries(byInstrId[0], byInstrId[1], "FCF to Debt"),
        ChartCreator.GLOBAL_OPTIONS_FOR_BILLIONS,
        "Chart"
    )
}