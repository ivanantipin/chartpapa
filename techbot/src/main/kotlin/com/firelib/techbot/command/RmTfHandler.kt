package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.persistence.TimeFrames
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.firelib.techbot.menu.fromUser
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class RmTfHandler : CommandHandler {

    companion object{
        val name = "rm_tf"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val timeFrame = cmd.opts["tf"]!!
        val fromUser = update.fromUser()

        val uid = fromUser.id

        BotHelper.ensureExist(fromUser)

        updateDatabase("update timeframes") {
            TimeFrames.deleteWhere { TimeFrames.user eq uid and (TimeFrames.tf eq timeFrame) }
        }.get()

        bot.sendMessage(
            chatId = ChatId.fromId(fromUser.id),
            text = BotHelper.displayTimeFrames(uid),
            parseMode = ParseMode.HTML
        )
    }
}

