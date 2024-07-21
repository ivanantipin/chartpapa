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
        val top = topCalculator.calculate(37, 7)

        val txt = "* Top 7 stocks, return for last 37 days *  \n" + top.joinToString(
            separator = "",
            transform = { pair -> " *${pair.first.code}* : ${dbl2Str(pair.second * 100, 2)} % \n" })

        bot.sendMessage(
            chatId = user.chatId(),
            text = txt,
            parseMode = ParseMode.MARKDOWN
        )

    }
}