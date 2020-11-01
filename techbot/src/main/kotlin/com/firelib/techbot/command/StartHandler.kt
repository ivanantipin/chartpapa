package com.firelib.techbot.command

import com.firelib.techbot.BotHelper.ensureExist
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class StartHandler : CommandHandler {


    override fun commands(): List<String> {
        return listOf("/start")
    }

    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {

        val fromUser = update.message!!.from!!

        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            ensureExist(fromUser)

            val mdText = """
                    [Инструкция](https://teletype.in/@techbot/techBotInstruction1)
                """.trimIndent()

            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = mdText,
                parseMode = ParseMode.MARKDOWN_V2
            )
        }
    }


    override fun description(): String {
        return "start comand"
    }
}