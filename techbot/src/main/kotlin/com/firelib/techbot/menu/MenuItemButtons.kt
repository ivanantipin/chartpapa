package com.firelib.techbot.menu

import com.firelib.techbot.Langs
import com.firelib.techbot.MsgEnum
import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.User

class MenuItemButtons(
    val name: MsgEnum,
    val buttons: MutableList<BotButton> = mutableListOf(),
    var title: (lang: Langs) -> String,
    var rowSize: Int = 3
) : BotMenu {

    override fun name(): MsgEnum {
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

    fun addActionButton(buttonName: String, action: suspend (bot: Bot, update: User) -> Unit) {
        buttons += ActionButton(buttonName, Cmd(getCmdName()), action)
    }

}