package com.firelib.techbot.command

import com.firelib.techbot.MsgEnum
import com.firelib.techbot.menu.MenuRegistry
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.staticdata.InstrumentsService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.User

class ListAddSymbolsHandler(
    val instrumentsService: InstrumentsService,
) {

    fun handle(txt: String, bot: Bot, user: User) {

        val filtered = instrumentsService.id2inst.values.filter {
            it.name.contains(txt, ignoreCase = true) || it.code.contains(
                txt,
                ignoreCase = true
            )
        }
        val buttons = filtered
            .map { SubscribeHandler.makeSubscribeButton(it) }

        if(filtered.size > 50){
            bot.sendMessage(
                user.chatId(),
                text = "Too many symbols, please narrow the search",
                parseMode = ParseMode.MARKDOWN
            )
        }else{
            MenuRegistry.listManyButtons(buttons, bot, user.chatId(), 1, MsgEnum.ChooseCompanyToSubscribe.toLocal(user.langCode()))
        }

    }
}