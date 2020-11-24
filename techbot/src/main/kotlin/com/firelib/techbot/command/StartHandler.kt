package com.firelib.techbot.command

import com.firelib.techbot.BotHelper.ensureExist
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class StartHandler : CommandHandler {

    override fun category() : CommandCategory{
        return CommandCategory.Other
    }



    override fun command(): String {
        return "/start"
    }

    override fun handle(cmd: Command, bot: Bot, update: Update) {

        val fromUser = update.message!!.from!!

        ensureExist(fromUser)

        val mdText = """
                    [Инструкция](https://teletype.in/@techbot/techBotInstruction3)
                """.trimIndent()

        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = mdText,
            parseMode = ParseMode.MARKDOWN
        )
    }

    override fun description(): String {
        return "start comand"
    }
}