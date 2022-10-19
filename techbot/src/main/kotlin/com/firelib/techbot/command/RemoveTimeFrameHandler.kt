package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.persistence.TimeFrames
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class RemoveTimeFrameHandler : CommandHandler {

    companion object {
        val name = "rm_tf"
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
            TimeFrames.deleteWhere { TimeFrames.user eq uid and (TimeFrames.tf eq timeFrame) }
        }.thenAccept({
            bot.sendMessage(
                chatId = ChatId.fromId(fromUser.id),
                text = BotHelper.displayTimeFrames(update),
                parseMode = ParseMode.MARKDOWN_V2
            )
        })

    }
}
