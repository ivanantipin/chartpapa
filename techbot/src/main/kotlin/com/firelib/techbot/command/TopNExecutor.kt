package com.firelib.techbot.command

import com.firelib.techbot.TopCalculator
import com.firelib.techbot.marketdata.OhlcsService
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.staticdata.InstrumentsService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.User
import firelib.core.misc.dbl2Str

class TopNExecutor(val ohlcService: OhlcsService, val instrumentsService: InstrumentsService) {

    companion object {
        val name = "top_n"
    }

    val topCalculator = TopCalculator(ohlcService, instrumentsService)

    fun handle(bot: Bot, user: User) {
        val cals = listOf(
            Triple(37,10, false),
            Triple(60,10, false),
            Triple(37,10, true),
            Triple(60,10, true),

        )
        cals.forEach {
            val top = topCalculator.calculate(it.first, it.second, it.third)

            val txt = "* ${ if(it.third) "Worst" else "Best" } ${it.second} stocks, return for last ${it.first} days *  \n" + top.joinToString(
                separator = "",
                transform = { pair -> " *${pair.first.code}* : ${dbl2Str(pair.second * 100, 2)} % \n" })

            bot.sendMessage(
                chatId = user.chatId(),
                text = txt,
                parseMode = ParseMode.MARKDOWN
            )
        }


    }
}