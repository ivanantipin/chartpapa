package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.persistence.TimeFrames
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class RemoveTimeFrameHandler : CommandHandler {

    companion object {
        val name = "rm_tf"
    }

    override fun command(): String {
        return name
    }

    override suspend fun handle(cmd: Cmd, bot: Bot, update: User) {
        val timeFrame = cmd.opts["tf"]!!

        DbHelper.ensureExist(update)

        DbHelper.updateDatabase("update timeframes") {
            TimeFrames.deleteWhere { TimeFrames.user eq update.id and (TimeFrames.tf eq timeFrame) }
        }
        bot.sendMessage(
            chatId = update.chatId(),
            text = BotHelper.displayTimeFrames(update),
            parseMode = ParseMode.MARKDOWN_V2
        )

    }
}

