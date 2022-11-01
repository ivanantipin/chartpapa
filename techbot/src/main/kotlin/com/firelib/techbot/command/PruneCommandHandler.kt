package com.firelib.techbot.command

import com.firelib.techbot.mainLogger
import com.firelib.techbot.marketdata.OhlcsService
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.staticdata.InstrumentsService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User

class PruneCommandHandler(
    val instrumentsService: InstrumentsService,
    val ohlcsService: OhlcsService
) : CommandHandler {

    override fun command(): String {
        return "prune"
    }

    override suspend fun handle(cmd: Cmd, bot: Bot, update: User) {
        mainLogger.info("pruning ${cmd}")
        val instr = cmd.instr(instrumentsService)

        bot.sendMessage(
            chatId = update.chatId(),
            text = "pruning ${instr}",
            parseMode = ParseMode.MARKDOWN
        )
        ohlcsService.prune(instr)
        bot.sendMessage(
            chatId = update.chatId(),
            text = "pruned ${instr}",
            parseMode = ParseMode.MARKDOWN
        )
        mainLogger.info("pruned instrument ${instr} by ${update}")

    }
}