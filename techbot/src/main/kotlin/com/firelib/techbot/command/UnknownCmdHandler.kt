package com.firelib.techbot.command

import com.firelib.techbot.BotHelper.checkTicker
import com.firelib.techbot.SymbolsDao
import com.firelib.techbot.TABot
import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update

class UnknownCmdHandler(val taBot: TABot) : CommandHandler {
    override fun commands(): List<String> {
        return listOf("")
    }

    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {
        if (SymbolsDao.available().find { it.code.equals(cmd.cmd, true) } != null) {

            var tf = TimeFrame.H
            if(cmd.opts.size > 0 && TimeFrame.values().find { it.name.equals(cmd.opts[0], true) } != null ){
                tf = TimeFrame.values().find { it.name.equals(cmd.opts[0], true) }!!
            }

            TrendsCommand().handle(Command("/tl", listOf(cmd.cmd, tf.name)), bot, update)
            DemarkCommand().handle(Command("/demark", listOf(cmd.cmd, tf.name)), bot, update)
        }else{
            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = "Unknown command, available commands:",
                parseMode = ParseMode.MARKDOWN
            )
            HelpListHandler(taBot).handle(cmd, bot, update)
        }
    }

    override fun description(): String {
        return "display trend lines"
    }
}