package com.firelib.techbot.command

import com.firelib.techbot.menu.chatId
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.persistence.SignalTypes
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.User
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

    override suspend fun handle(cmd: Cmd, bot: Bot, user: User) {
        val signalType = cmd.opts[SIGNAL_TYPE_ATTRIBUTE]!!

        DbHelper.ensureExist(user)

        val flag = DbHelper.updateDatabase("update signal type") {
            if (SignalTypes.select { SignalTypes.user eq user.id and (SignalTypes.signalType eq signalType) }
                    .empty()) {
                SignalTypes.insert {
                    it[SignalTypes.user] = user.id
                    it[SignalTypes.signalType] = signalType
                }
                true
            } else {
                false
            }

        }
        if (flag) {
            bot.sendMessage(
                chatId = user.chatId(),
                text = "Тип сигнала добавлен в нотификации ${signalType}",
                parseMode = ParseMode.MARKDOWN
            )
        }

    }
}