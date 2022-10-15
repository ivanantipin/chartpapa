package com.firelib.techbot.command

import com.firelib.techbot.mainLogger
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.staticdata.InstrumentsService
import com.firelib.techbot.staticdata.OhlcsService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update

class PruneCommandHandler(
    val instrumentsService: InstrumentsService,
    val ohlcsService: OhlcsService
) : CommandHandler {

    override fun command(): String {
        return "prune"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        mainLogger.info("pruning ${cmd}")
        val instr = cmd.instr(instrumentsService)
        ohlcsService.prune(instr)
        val fromUser = update.fromUser()
        bot.sendMessage(
            chatId = ChatId.fromId(update.fromUser().id),
            text = "pruned ${instr}",
            parseMode = ParseMode.MARKDOWN
        )
        mainLogger.info("pruned instrument ${instr} by ${fromUser}")

    }
}