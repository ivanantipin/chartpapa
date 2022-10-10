package com.firelib.techbot.menu

import com.firelib.techbot.Langs
import com.firelib.techbot.MsgLocalazer
import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

class MenuItemButtons(
    val name: MsgLocalazer,
    val buttons: MutableList<IButton> = mutableListOf(),
    var title : (lang : Langs)->String,
    var rowSize: Int = 3
) : IMenu {

    override fun name(): MsgLocalazer {
        return name
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = { bot, update ->
            registry.listUncat(buttons, bot, update.chatId(), rowSize,  title(update.langCode()))
        }
        buttons.forEach { it.register(registry) }
    }

    fun addButton(chName: String, buttonTitle: (Langs)->String, aa: StaticButtonParent.() -> Unit) {
        buttons += StaticButtonParent(chName, Cmd(getCmdName()), buttonTitle).apply(aa)
    }

    fun addActionButton(buttonName: String, action: (bot: Bot, update: Update) -> Unit) {
        buttons += ActionButton(buttonName, Cmd(getCmdName()), action)
    }

}