package com.firelib.techbot.command

import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.persistence.TimeFrames
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class TimeFrameHandler : CommandHandler {

    companion object {
        val name = "add_tf"
    }

    override fun command(): String {
        return name
    }

    override suspend fun handle(cmd: Cmd, bot: Bot, user: User) {
        val timeFrame = cmd.opts["tf"]!!

        DbHelper.ensureExist(user)

        val flag = DbHelper.updateDatabase("update timeframes") {
            if (TimeFrames.select { TimeFrames.user eq user.id and (TimeFrames.tf eq timeFrame) }
                    .empty()) {
                TimeFrames.insert {
                    it[this.user] = user.id
                    it[tf] = timeFrame
                }
                true
            } else {
                false
            }
        }
        if (flag) {
            bot.sendMessage(
                chatId = user.chatId(),
                text = MsgLocalizer.TimeFrameAdded.toLocal(user.langCode()),
                parseMode = ParseMode.MARKDOWN
            )
        }
    }
}

