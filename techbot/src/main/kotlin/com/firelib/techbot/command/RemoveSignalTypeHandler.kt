package com.firelib.techbot.command

import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.command.SignalTypeHandler.Companion.SIGNAL_TYPE_ATTRIBUTE
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.persistence.SignalTypes
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class RemoveSignalTypeHandler : CommandHandler {

    companion object {
        val name = "rms"
    }

    override fun command(): String {
        return name
    }

    override suspend fun handle(cmd: Cmd, bot: Bot, user: User) {
        val signalType = cmd.opts[SIGNAL_TYPE_ATTRIBUTE]!!
        DbHelper.ensureExist(user)
        DbHelper.updateDatabase("update signal type") {
            SignalTypes.deleteWhere { SignalTypes.user eq user.id and (SignalTypes.signalType eq signalType) }
        }

        bot.sendMessage(
            chatId = user.chatId(),
            text = MsgLocalizer.SubscrptionRemoved.toLocal(user.langCode()) + signalType,
            parseMode = ParseMode.HTML
        )

    }
}