package com.firelib.techbot.command

import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.UserId
import com.firelib.techbot.getId
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.staticdata.InstrumentsService
import com.firelib.techbot.subscriptions.SubscriptionService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update

class UnsubscribeHandler(val staticDataService: InstrumentsService, val subscriptionService: SubscriptionService) :
    CommandHandler {
    companion object {
        val name = "unsub"
    }

    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val instrId = cmd.instr(staticDataService)
        val uid = update.chatId()
        subscriptionService.deleteSubscription(UserId(uid.getId()), instrId)
        bot.sendMessage(
            chatId = uid,
            text = MsgLocalizer.SubscrptionRemoved.toLocal(update.langCode()) + instrId.code,
            parseMode = ParseMode.MARKDOWN
        )
    }
}