package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

class MenuItemButtons(val name: String,
                      val buttons: MutableList<InlineButton> = mutableListOf(),
                      var title: String = name,
                      var rowSize: Int = 3
) : IMenu {
    override fun name(): String {
        return name
    }

    override fun act(bot: Bot, update: Update) {
        MenuRegistry.list(buttons.chunked(rowSize), bot, update.chatId(), title)
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = {bot, update->
            registry.listUncat(buttons, bot, update.chatId(), rowSize)
        }
        buttons.forEach { registry.registerButton(it) }
    }

    fun inlButton(chName: String, data: Cmd, title: String = "", aa: InlineButton.() -> Unit): InlineButton {
        val ret = InlineButton(chName, data, title)
        aa(ret)
        buttons += ret
        return ret
    }


    fun parentInlButton(chName: String, aa: InlineButton.() -> Unit): InlineButton {
        val ret = InlineButton(chName, Cmd(getCmdName()), title)
        aa(ret)
        buttons += ret
        return ret
    }

}