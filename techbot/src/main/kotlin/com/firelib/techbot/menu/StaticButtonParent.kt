package com.firelib.techbot.menu

import com.firelib.techbot.Langs
import com.firelib.techbot.command.Cmd

class StaticButtonParent(
    override val name: String,
    override val data: Cmd,
    val title: (Langs) -> String,
    var rowSize: Int = 3
) : BotButton {
    val buttons: MutableList<BotButton> = mutableListOf()

    override fun children(): List<BotButton> {
        return buttons
    }

    override fun register(menuRegistry: MenuRegistry) {
        menuRegistry.commandData[data.handlerName] = { cmd, bot, update ->
            MenuRegistry.listManyButtons(buttons, bot, update.chatId(), rowSize, title(update.langCode()))
        }
        buttons.forEach { it.register(menuRegistry) }
    }

}