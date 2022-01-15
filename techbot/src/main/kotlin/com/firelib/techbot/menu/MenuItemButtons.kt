package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

class MenuItemButtons(
    val name: String,
    val buttons: MutableList<IButton> = mutableListOf(),
    var title: String = name,
    var rowSize: Int = 3
) : IMenu {
    override fun name(): String {
        return name
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = { bot, update ->
            registry.listUncat(buttons, bot, update.chatId(), rowSize, title)
        }
        buttons.forEach { it.register(registry) }
    }

    fun addButton(chName: String, buttonTitle: String, aa: StaticButtonParent.() -> Unit) {
        buttons += StaticButtonParent(chName, Cmd(getCmdName()), buttonTitle).apply(aa)
    }

    fun addActionButton(buttonName: String, action: (bot: Bot, update: Update) -> Unit) {
        buttons += ActionButton(buttonName, Cmd(getCmdName()), action)
    }

}