package com.firelib.techbot.command

import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.persistence.TimeFrames
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
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

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val timeFrame = cmd.opts["tf"]!!
        val fromUser = update.fromUser()

        val uid = fromUser.id

        DbHelper.ensureExist(fromUser)

        DbHelper.updateDatabase("update timeframes") {
            if (TimeFrames.select { TimeFrames.user eq uid and (TimeFrames.tf eq timeFrame) }
                    .empty()) {
                TimeFrames.insert {
                    it[user] = uid
                    it[tf] = timeFrame
                }
                true
            } else {
                false
            }
        }.thenAccept { flag ->
            if (flag) {
                bot.sendMessage(
                    chatId = ChatId.fromId(fromUser.id),
                    text = MsgLocalizer.TimeFrameAdded.toLocal(update.langCode()),
                    parseMode = ParseMode.MARKDOWN
                )
            }
        }
    }
}
