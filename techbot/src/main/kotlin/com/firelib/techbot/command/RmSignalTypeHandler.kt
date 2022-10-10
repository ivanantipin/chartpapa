package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.command.SignalTypeHandler.Companion.SIGNAL_TYPE_ATTRIBUTE
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.persistence.SignalTypes
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class RmSignalTypeHandler : CommandHandler {

    companion object {
        val name = "rms"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val signalType = cmd.opts[SIGNAL_TYPE_ATTRIBUTE]!!
        val fromUser = update.fromUser()
        BotHelper.ensureExist(fromUser)
        updateDatabase("update signal type") {
            SignalTypes.deleteWhere { SignalTypes.user eq fromUser.id and (SignalTypes.signalType eq signalType) }
        }.thenAccept({
            bot.sendMessage(
                chatId = ChatId.fromId(fromUser.id),
                text = MsgLocalizer.SubscrptionRemoved.toLocal(update.langCode()) + signalType,
                parseMode = ParseMode.HTML
            )
        })

    }
}