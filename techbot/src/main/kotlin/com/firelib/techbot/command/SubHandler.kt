package com.firelib.techbot.command

import com.firelib.techbot.*
import com.firelib.techbot.BotHelper.ensureExist
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.staticdata.StaticDataService
import com.firelib.techbot.staticdata.SubscriptionService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update

class SubHandler(val subscriptionService: SubscriptionService, val staticDataService: StaticDataService) : CommandHandler {

    companion object{
        val name = "sub"
    }


    override fun command(): String {
        return name
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val instr = cmd.instr(staticDataService)
        val fromUser = update.fromUser()
        val uid = fromUser.id
        ensureExist(fromUser)
        if(subscriptionService.addSubscription(UserId(uid), instr)){
            bot.sendMessage(
                chatId = ChatId.fromId(fromUser.id),
                text = "Добавлен символ ${instr.code} ",
                parseMode = ParseMode.MARKDOWN
            )
        }
    }
}




