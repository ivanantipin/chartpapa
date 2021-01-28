package com.firelib.techbot.command

import com.firelib.techbot.*
import com.firelib.techbot.BotHelper.ensureExist
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Cmd
import com.github.kotlintelegrambot.dispatcher.fromUser
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select


class SubHandler : CommandHandler {
    override fun command(): String {
        return "sub"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val instr = cmd.instr()
        val fromUser = update.fromUser()

        val uid = fromUser.id.toInt()

        ensureExist(fromUser)

        var added = false
        updateDatabase("update subscription") {
            if (Subscriptions.select { Subscriptions.user eq uid and (Subscriptions.ticker eq instr.code) }
                    .empty()) {
                Subscriptions.insert {
                    it[user] = uid
                    it[ticker] = instr.code
                    it[Subscriptions.market] = instr.market
                }
                added = true;
            }
        }.get()

        var mdAvailable = true

        val fut = MdService.update(instr)
        if (fut != null) {
            fut.thenAccept {

                UpdateSensitivities.updateSens(instr).get()
                UpdateLevelsSensitivities.updateTicker(instr).get()
            }
            mdAvailable = false
        }

        val msg =
            if (mdAvailable) "" else " маркет данные будут вскоре обновлены, графики могут быть недоступны в течении некоторого времени"


        if (added) {
            bot.sendMessage(
                chatId = fromUser.id,
                text = "Добавлен символ ${instr.code}, ${msg}",
                parseMode = ParseMode.MARKDOWN
            )
        }
    }
}




