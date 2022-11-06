package com.firelib.techbot.command

import com.firelib.techbot.MsgEnum
import com.firelib.techbot.menu.*
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.staticdata.InstrumentsService
import com.firelib.techbot.subscriptions.SubscriptionService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.User
import firelib.core.domain.InstrId

class SubscribeHandler(val subscriptionService: SubscriptionService, val staticDataService: InstrumentsService) :
    CommandHandler {

    companion object {
        val name = "sub"

        fun makeSubscribeButton(code : InstrId) : BotButton{
            return SimpleButton("(${code.code}) ${code.name} / ${code.source}", Cmd(name, mapOf("id" to code.id)))
        }
    }

    override fun command(): String {
        return name
    }

    override suspend fun handle(cmd: Cmd, bot: Bot, user: User) {
        val instr = cmd.instr(staticDataService)
        DbHelper.ensureExist(user)
        if (subscriptionService.addSubscription(user.userId(), instr)) {
            bot.sendMessage(
                chatId = user.chatId(),
                text = "${MsgEnum.Subscribed.toLocal(user.langCode())} ${instr}",
                parseMode = ParseMode.MARKDOWN
            )
        }
    }
}




