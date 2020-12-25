package com.firelib.techbot.command

import com.firelib.techbot.Subscriptions
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Cmd
import com.github.kotlintelegrambot.dispatcher.chatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class RmHandler : CommandHandler {

    override fun command(): String {
        return "unsub"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val tkr = cmd.opts["ticker"]!!
        val uid = update.chatId().toInt()
        var cnt = 0;
        updateDatabase("delete user", {
            cnt = Subscriptions.deleteWhere { Subscriptions.user eq uid and (Subscriptions.ticker eq tkr) }
        }).get()
        if (cnt > 0) {
            bot.sendMessage(
                chatId = uid.toLong(),
                text = "Символ удален ${tkr}",
                parseMode = ParseMode.MARKDOWN
            )
        }
    }
}