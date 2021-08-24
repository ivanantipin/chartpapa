package com.firelib.techbot.command

import chart.BreachFinder
import com.firelib.techbot.Cmd
import com.firelib.techbot.chart.*
import com.firelib.techbot.command.FundamentalService.mergeAndSort
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.initDatabase
import com.firelib.techbot.saveFile
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.chatId
import com.github.kotlintelegrambot.entities.Update
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.store.MdStorageImpl
import java.io.File
import java.math.BigDecimal


class FundamentalsCommand : CommandHandler {


    val mdStorageImpl = MdStorageImpl()

    val colors = mapOf<String, String>(
        "operatingIncome" to "green",
        "costOfRevenue" to "FF618C",
        "totalOperatingExpenses" to "lightBlue",

        )

    val actions = mapOf(
        "earnings" to { instrId: InstrId ->
            val series = FundamentalService.getByInstrId(instrId)
            ChartService.post(
                OperatingStructureCharter.makeSeries(mergeAndSort(series), instrId.code, colors),
                ChartCreator.GLOBAL_OPTIONS_FOR_BILLIONS,
                "Chart"
            )
        },

        "fcf2debt" to { instrId ->
            val fcf2debt = FundamentalService.getFcfToDebt(instrId)
            ChartService.post(
                Fcf2DebtCharter.makeSeries(fcf2debt[0], fcf2debt[1], instrId.code),
                ChartCreator.GLOBAL_OPTIONS_FOR_BILLIONS,
                "Chart"
            )
        },

        "evToEbitda" to { instrId ->
            val evEbit = FundamentalService.getEv(instrId, mdStorageImpl)
            ChartService.post(
                EvEbitdaCharter.makeSeries(evEbit[0], evEbit[1], instrId.code),
                ChartCreator.GLOBAL_OPTIONS_FOR_BILLIONS,
                "Chart"
            )
        }


    )

    override fun command(): String {
        return "fund"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {

        val instrId = cmd.instr()

        actions.forEach({ ee ->
            val bytes = ee.value(instrId)
            val fileName = BreachFinder.makeSnapFileName(
                ee.key,
                instrId.code,
                TimeFrame.D,
                System.currentTimeMillis()
            )
            saveFile(bytes, fileName)
            bot.sendPhoto(chatId = update.chatId(), photo = File(fileName))
        })

    }
}

fun main() {

    initDatabase()
    val instrument = InstrId.dummyInstrument("KOS").copy(source = SourceName.EODHIST.name, market = "NYSE")

    val cmd = FundamentalsCommand()

    val evEbit = FundamentalService.getEv(instrument, MdStorageImpl())

    ChartService.post(
        EvEbitdaCharter.makeSeries(evEbit[0], evEbit[1], "ev-ebitda"),
        ChartCreator.GLOBAL_OPTIONS_FOR_BILLIONS,
        "Chart"
    )


}