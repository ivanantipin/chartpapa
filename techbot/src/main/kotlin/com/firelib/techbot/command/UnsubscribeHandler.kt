package com.firelib.techbot.command

import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.getId
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.menu.userId
import com.firelib.techbot.staticdata.InstrumentsService
import com.firelib.techbot.subscriptions.SubscriptionService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User

class UnsubscribeHandler(val staticDataService: InstrumentsService, val subscriptionService: SubscriptionService) :
    CommandHandler {
    companion object {
        val name = "unsub"
    }

    override fun command(): String {
        return name
    }

    override suspend fun handle(cmd: Cmd, bot: Bot, user: User) {
        val instrId = cmd.instr(staticDataService)
        subscriptionService.deleteSubscription(user.userId(), instrId)
        bot.sendMessage(
            chatId = user.chatId(),
            text = MsgLocalizer.SubscrptionRemoved.toLocal(user.langCode()) + instrId.code,
            parseMode = ParseMode.MARKDOWN
        )
    }
}