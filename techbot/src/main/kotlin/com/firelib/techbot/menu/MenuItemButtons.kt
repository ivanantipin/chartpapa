package com.firelib.techbot.menu

import com.firelib.techbot.Langs
import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User

class MenuItemButtons(
    val name: MsgLocalizer,
    val buttons: MutableList<BotButton> = mutableListOf(),
    var title: (lang: Langs) -> String,
    var rowSize: Int = 3
) : BotMenu {

    override fun name(): MsgLocalizer {
        return name
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = { bot, update ->
            MenuRegistry.listManyButtons(buttons, bot, update.chatId(), rowSize, title(update.langCode()))
        }
        buttons.forEach { it.register(registry) }
    }

    fun addButton(chName: String, buttonTitle: (Langs) -> String, aa: StaticButtonParent.() -> Unit) {
        buttons += StaticButtonParent(chName, Cmd(getCmdName()), buttonTitle).apply(aa)
    }

    fun addActionButton(buttonName: String, action: (bot: Bot, update: User) -> Unit) {
        buttons += ActionButton(buttonName, Cmd(getCmdName()), action)
    }

}