package com.firelib.techbot.command

import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.persistence.Users
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import org.jetbrains.exposed.sql.update

class LanguageChangeHandler : CommandHandler {

    companion object {
        val name = "setLang"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: User) {
        val lang = cmd.opts["lang"]!!
        DbHelper.ensureExist(update)
        DbHelper.updateDatabase("update language") {
            Users.update({ Users.userId eq update.id }) {
                it[Users.lang] = lang
            }
        }.thenAccept({
            bot.sendMessage(
                chatId = update.chatId(),
                text = "User language set to ${lang}",
                parseMode = ParseMode.MARKDOWN_V2
            )
        })

    }
}

