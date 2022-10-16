package com.firelib.techbot.command

import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.persistence.SignalTypes
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class SignalTypeHandler : CommandHandler {

    companion object {
        val name = "addS"
        val SIGNAL_TYPE_ATTRIBUTE = "sT"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val signalType = cmd.opts[SIGNAL_TYPE_ATTRIBUTE]!!
        val fromUser = update.fromUser()

        val uid = fromUser.id

        DbHelper.ensureExist(fromUser)

        DbHelper.updateDatabase("update signal type") {
            if (SignalTypes.select { SignalTypes.user eq uid and (SignalTypes.signalType eq signalType) }
                    .empty()) {
                SignalTypes.insert {
                    it[SignalTypes.user] = uid
                    it[SignalTypes.signalType] = signalType
                }
                true
            } else {
                false
            }

        }.thenAccept { flag ->
            if (flag) {
                bot.sendMessage(
                    chatId = ChatId.fromId(fromUser.id),
                    text = "Тип сигнала добавлен в нотификации ${signalType}",
                    parseMode = ParseMode.MARKDOWN
                )
            }
        }

    }
}