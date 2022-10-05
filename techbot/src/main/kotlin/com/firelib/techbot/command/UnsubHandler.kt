package com.firelib.techbot.command

import com.firelib.techbot.Msg
import com.firelib.techbot.getId
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.persistence.Subscriptions
import com.firelib.techbot.staticdata.StaticDataService
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class UnsubHandler(val staticDataService: StaticDataService) : CommandHandler {
    companion object {
        val name = "unsub"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val instrId = cmd.instr(staticDataService)
        val uid = update.chatId()

        updateDatabase("delete user", {
            Subscriptions.deleteWhere { Subscriptions.user eq uid.getId() and (Subscriptions.ticker eq instrId.code) }
        }).thenAccept {
            bot.sendMessage(
                chatId = uid,
                text = Msg.SubscrptionRemoved.toLocal(update.langCode()) + instrId.code,
                parseMode = ParseMode.MARKDOWN
            )
        }
    }
}