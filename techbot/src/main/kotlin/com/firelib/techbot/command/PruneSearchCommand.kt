package com.firelib.techbot.command

import com.firelib.techbot.menu.MenuRegistry
import com.firelib.techbot.menu.SimpleButton
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.staticdata.InstrumentsService
import com.firelib.techbot.subscriptions.SubscriptionService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

class PruneSearchCommand(val instrumentService : InstrumentsService, val subscriptionService: SubscriptionService ) : TextCommand {
    override fun name(): String {
        return "/list"
    }

    override fun displaySettings(bot: Bot, userId: Long) {

    }

    override fun handle(cmd: List<String>, bot: Bot, update: Update) {
        val liveInstruments = subscriptionService.liveInstruments().toSet()
        val filter = instrumentService.id2inst.values.filter {
            it.code.contains(cmd[1], ignoreCase = true) || it.name.contains(
                cmd[1],
                ignoreCase = true
            )
        }.filter { liveInstruments.contains(it) }

        val bts = filter.map { instrId ->
            SimpleButton(
                instrId.code,
                Cmd("prune", mapOf("id" to instrId.id))
            )
        }
        MenuRegistry.listManyButtons(bts, bot, update.chatId(), 4)
    }
}