package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User

class ActionButton(
    override val name: String,
    override val data: Cmd,
    val action: ((bot: Bot, update: User) -> Unit)
) : BotButton {

    override fun children(): List<BotButton> {
        return emptyList()
    }

    override fun register(menuRegistry: MenuRegistry) {
        menuRegistry.commandData[data.handlerName] = { cmd, bot, update -> action(bot, update) }
    }
}