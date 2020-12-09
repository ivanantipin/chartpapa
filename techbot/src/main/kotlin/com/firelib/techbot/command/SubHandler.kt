package com.firelib.techbot.command

import com.firelib.techbot.BotHelper.displaySubscriptions
import com.firelib.techbot.BotHelper.ensureExist
import com.firelib.techbot.Subscriptions
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Cmd
import com.github.kotlintelegrambot.dispatcher.fromUser
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select


class SubHandler : CommandHandler {
    override fun command(): String {
        return "sub"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val tkr = cmd.opts["ticker"]!!
        val fromUser = update.fromUser()

        val uid = fromUser.id.toInt()

        ensureExist(fromUser)

        updateDatabase("update subscription") {
            if (Subscriptions.select { Subscriptions.user eq uid and (Subscriptions.ticker eq tkr) }
                    .empty()) {
                Subscriptions.insert {
                    it[user] = uid
                    it[ticker] = tkr
                }
            }
        }.get()

        bot.sendMessage(
            chatId = fromUser.id,
            text = displaySubscriptions(uid),
            parseMode = ParseMode.MARKDOWN
        )
    }
}




