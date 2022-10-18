package com.firelib.techbot.command

import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.SignalType
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.persistence.Settings
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.core.misc.JsonHelper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

interface TextCommand{
    fun name() : String
    fun displaySettings(
        bot: Bot,
        userId: Long
    )
    fun handle(cmd: List<String>, bot: Bot, update: Update)
}

class SettingsCommand : TextCommand{

    val name = "/set"

    override fun displaySettings(
        bot: Bot,
        userId: Long
    ) {

        val header = "*_Ваши установки\n\n_*"

        val txt = DbHelper.readSettings(userId).joinToString(separator = "\\-\\-\\-", transform = {
            "\\-\\-\\-_*${it["command"]!!.uppercase()}*_\\-\\-\\-\n" + it.entries.filter { it.key != "command" }
                .joinToString("\n", transform = { entry -> "*${entry.key}* : _${entry.value}_" })
        })

        bot.sendMessage(
            chatId = ChatId.fromId(userId.toLong()),
            text = header + txt,
            parseMode = ParseMode.MARKDOWN_V2
        )
    }

    override fun name() : String{
        return "/set"
    }

    override fun handle(cmd: List<String>, bot: Bot, update: Update) {

        val fromUser = update.fromUser()

        val uid = fromUser.id

        val signalType = SignalType.values().find { cmd[1] == it.name }

        if (signalType == null) {
            bot.sendMessage(
                chatId = ChatId.fromId(uid.toLong()),
                text = "Неизвестная команда",
                parseMode = ParseMode.MARKDOWN_V2
            )
            return
        }

        if (!signalType.signalGenerator.validate(cmd)) {
            signalType.signalGenerator.displayHelp(bot, update)
            return
        }
        val parsed = signalType.signalGenerator.parsePayload(cmd)

        val settingsJson = JsonHelper.toJsonString(parsed)

        DbHelper.updateDatabase("update subscription") {
            val recs: Int = Settings.deleteWhere {
                Settings.user eq uid and (Settings.name eq signalType.name)
            }
            Settings.insert {
                it[user] = uid
                it[name] = parsed["command"]!!
                it[value] = settingsJson
            }
            recs > 0
        }.thenAccept {
            displaySettings(bot, fromUser.id)
        }
    }

}