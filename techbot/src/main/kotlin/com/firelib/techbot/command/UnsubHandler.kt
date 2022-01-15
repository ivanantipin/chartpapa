package com.firelib.techbot.command

import com.firelib.techbot.persistence.Subscriptions
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.firelib.techbot.getId
import com.firelib.techbot.menu.chatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class UnsubHandler : CommandHandler {
    companion object{
        val name = "unsub"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val instrId = cmd.instr()
        val uid = update.chatId()
        val cnt = updateDatabase("delete user", {
            Subscriptions.deleteWhere { Subscriptions.user eq uid.getId().toInt() and (Subscriptions.ticker eq instrId.code) }
        }).get()
        if (cnt > 0) {
            bot.sendMessage(
                chatId = uid,
                text = "Символ удален ${instrId}",
                parseMode = ParseMode.MARKDOWN
            )
        }
    }
}