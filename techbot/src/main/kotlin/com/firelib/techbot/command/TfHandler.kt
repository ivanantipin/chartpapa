package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.TimeFrames
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.firelib.techbot.menu.fromUser
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class TfHandler : CommandHandler {
    override fun command(): String {
        return "add_tf"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val timeFrame = cmd.opts["tf"]!!
        val fromUser = update.fromUser()

        val uid = fromUser.id.toInt()

        BotHelper.ensureExist(fromUser)

        var flag = false

        updateDatabase("update timeframes") {
            if (TimeFrames.select { TimeFrames.user eq uid and (TimeFrames.tf eq timeFrame) }
                    .empty()) {
                TimeFrames.insert {
                    it[TimeFrames.user] = uid
                    it[TimeFrames.tf] = timeFrame
                }
                flag = true
            }
        }.get()

        if (flag) {
            bot.sendMessage(
                chatId = ChatId.fromId(fromUser.id),
                text = "Таймфрейм добавлен ${timeFrame}",
                parseMode = ParseMode.MARKDOWN
            )
        }
    }
}