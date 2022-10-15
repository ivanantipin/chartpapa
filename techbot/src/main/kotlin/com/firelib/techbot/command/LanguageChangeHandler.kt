package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.persistence.Users
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.update

class LanguageChangeHandler : CommandHandler {

    companion object {
        val name = "setLang"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val lang = cmd.opts["lang"]!!
        val fromUser = update.fromUser()

        val uid = fromUser.id

        BotHelper.ensureExist(fromUser)

        updateDatabase("update language") {
            Users.update({ Users.userId eq uid }) {
                it[Users.lang] = lang
            }
        }.thenAccept({
            bot.sendMessage(
                chatId = ChatId.fromId(fromUser.id),
                text = "User language set to ${lang}",
                parseMode = ParseMode.MARKDOWN_V2
            )
        })

    }
}

