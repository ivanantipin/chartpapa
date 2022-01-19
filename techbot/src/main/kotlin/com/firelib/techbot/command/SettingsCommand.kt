package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.persistence.Settings
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import firelib.core.misc.JsonHelper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class SettingsCommand {

    companion object {
        val name = "/set"

        fun displaySettings(
            bot: Bot,
            userId: Long
        ) {

            val header = "*_Ваши установки\n\n_*"

            val txt = BotHelper.readSettings(userId).joinToString(separator = "\\-\\-\\-", transform = {
                "\\-\\-\\-_*${it["command"]!!.uppercase()}*_\\-\\-\\-\n" + it.entries.filter { it.key != "command" }
                    .joinToString("\n", transform =  { entry -> "*${entry.key}* : _${entry.value}_" })
            })

            bot.sendMessage(
                chatId = ChatId.fromId(userId.toLong()),
                text = header + txt,
                parseMode = ParseMode.MARKDOWN_V2
            )
        }

    }

    fun handle(cmd: List<String>, bot: Bot, update: Update) {

        val fromUser = update.fromUser()

        val uid = fromUser.id.toInt()

        if (!MacdCommand.validate(cmd)) {
            MacdCommand.displayMACD_Help(bot, update)
            return
        }
        val parsed = MacdCommand.parsePayload(cmd.subList(1, cmd.size))
        val settingsJson = JsonHelper.toJsonString(parsed)

        updateDatabase("update subscription") {
            val recs: Int = Settings.deleteWhere {
                Settings.user eq uid and (Settings.name eq "macd")
            }
            Settings.insert {
                it[user] = uid
                it[name] = parsed["command"]!!
                it[value] = settingsJson
            }
            recs > 0
        }.get()

        displaySettings(bot, fromUser.id)

    }

}