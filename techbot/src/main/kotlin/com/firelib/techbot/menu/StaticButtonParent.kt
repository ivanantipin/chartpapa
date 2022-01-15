package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

interface IButton{
    val name : String
    val data : Cmd
    fun children() : List<IButton>
    fun register(menuRegistry: MenuRegistry)
}

class ActionButton(override val name: String, override val data: Cmd, val action: ((bot: Bot, update: Update) -> Unit)) : IButton{
    override fun children(): List<IButton> {
        return emptyList()
    }

    override fun register(menuRegistry: MenuRegistry) {
        menuRegistry.commandData[data.handlerName] = {cmd, bot, update->action(bot, update)}
    }
}

class SimpleButton(override val name: String, override val data: Cmd) : IButton{
    override fun children(): List<IButton> {
        return emptyList()
    }

    override fun register(menuRegistry: MenuRegistry) {
    }
}



class StaticButtonParent(override val name: String, override val data: Cmd, val title: String, var rowSize: Int = 3) : IButton{
    val buttons: MutableList<IButton> = mutableListOf()

    fun addButtonToButton(chName: String, ttl: String, aa: StaticButtonParent.() -> Unit) {
        buttons += StaticButtonParent(chName, Cmd(getCmdName()), ttl).apply(aa)
    }

    override fun children(): List<IButton> {
        return buttons
    }

    override fun register(menuRegistry: MenuRegistry) {
        menuRegistry.commandData[data.handlerName] = {cmd, bot, update->
            menuRegistry.listUncat(buttons, bot, update.chatId(), rowSize, title)
        }
        buttons.forEach { it.register(menuRegistry) }
    }

}