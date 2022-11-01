package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd
import com.firelib.techbot.mainLogger
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User

class ActionButton(
    override val name: String,
    override val data: Cmd,
    val action: suspend ((bot: Bot, update: User) -> Unit)
) : BotButton {

    override fun children(): List<BotButton> {
        return emptyList()
    }

    override fun register(menuRegistry: MenuRegistry) {
        menuRegistry.commandData[data.handlerName] = { cmd, bot, user ->
            mainLogger.info("executing action button ${name} for user ${user} data is ${data}")
            action(bot, user)
        }
    }
}