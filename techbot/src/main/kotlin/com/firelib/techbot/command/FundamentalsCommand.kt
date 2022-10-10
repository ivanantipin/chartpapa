package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.breachevent.BreachEvents.makeSnapFileName
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.chart.GenericCharter
import com.firelib.techbot.chart.RenderUtils
import com.firelib.techbot.chart.SeriesUX
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.mainLogger
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.staticdata.InstrumentsService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.InstrId
import firelib.core.store.MdStorageImpl
import java.io.File

class FundamentalsCommand(val staticDataService: InstrumentsService) : CommandHandler {

    companion object {
        val name = "fund"
    }

    val mdStorageImpl = MdStorageImpl()

    val colors = mapOf<String, String>(
        "equity_attributable_to_parent" to "green",
        "equity_attributable_to_noncontrolling_interest" to "blue",
        "noncurrent_liabilities" to "pink",
        "current_liabilities" to "red",
        "operatingIncome" to "green",
        "costOfRevenue" to "FF618C",
        "totalOperatingExpenses" to "lightBlue",

        "other_than_fixed_noncurrent_assets" to "blue",
        "fixed_assets" to "green",
        "current_assets" to "lightGreen",

        "net_cash_flow_from_investing_activities_continuing" to "lightGreen",
        "net_cash_flow_from_financing_activities" to "gray",
        "net_cash_flow_from_operating_activities" to "green",
        "exchange_gains_losses" to "blue"
    )

    val actions = mapOf(
        "balanceSheet" to { instrId: InstrId ->
            val fields = listOf(
                "equity_attributable_to_parent",
                "equity_attributable_to_noncontrolling_interest",
                "noncurrent_liabilities",
                "current_liabilities"
            )
            val series = FundamentalServicePoligon.getFromBalanceSheet(instrId, fields)
            ChartService.post(
                GenericCharter.makeSeries(series.map {
                    SeriesUX(it, colors[it.name]!!, 0, makeTicks = false)
                }, "Balance Sheet Structure ${instrId.code}", listOf("Money")),
                RenderUtils.GLOBAL_OPTIONS_FOR_BILLIONS,
                "Chart"
            )
        },
        "balanceSheet2" to { instrId: InstrId ->
            val fields = listOf("current_assets", "other_than_fixed_noncurrent_assets", "fixed_assets")
            val series = FundamentalServicePoligon.getFromBalanceSheet(instrId, fields)
            ChartService.post(
                GenericCharter.makeSeries(series.map {
                    SeriesUX(it, colors[it.name]!!, 0, makeTicks = false)
                }, "Balance Sheet Structure / 2 ${instrId.code}", listOf("Money")),
                RenderUtils.GLOBAL_OPTIONS_FOR_BILLIONS,
                "Chart"
            )
        },
        "cashFlowStructure" to { instrId: InstrId ->
            val fields = listOf(
                "net_cash_flow_from_investing_activities_continuing",
                "net_cash_flow_from_financing_activities",
                "net_cash_flow_from_operating_activities",
                "exchange_gains_losses"
            )
            val series = FundamentalServicePoligon.getFromCashFlow(instrId, fields)
            ChartService.post(
                GenericCharter.makeSeries(series.map {
                    SeriesUX(it, colors[it.name]!!, 0, makeTicks = false)
                }, "Cash Flow Structure ${instrId.code}", listOf("Money")),
                RenderUtils.GLOBAL_OPTIONS_FOR_BILLIONS,
                "Chart"
            )
        },
        "ev2ebitda" to { instrId: InstrId ->
            val series = FundamentalServicePoligon.ev2Ebitda(instrId, mdStorageImpl)
            ChartService.post(
                GenericCharter.makeSeries(series, "Ev2Ebitda ${instrId.code}", listOf("Ev2Ebitda")),
                RenderUtils.GLOBAL_OPTIONS_FOR_BILLIONS,
                "Chart"
            )
        },

        )

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {

        val instrId = cmd.instr(staticDataService)

        actions.forEach({ ee ->
            try {
                val bytes = ee.value(instrId)
                val fileName = makeSnapFileName(
                    ee.key,
                    instrId.code,
                    TimeFrame.D,
                    System.currentTimeMillis()
                )
                BotHelper.saveFile(bytes, fileName)
                bot.sendPhoto(chatId = update.chatId(), photo = File(fileName))
            } catch (e: Exception) {
                mainLogger.error("failed to execute fundamental action ${ee.key}  for cmd ${cmd}", e)
            }

        })

    }
}