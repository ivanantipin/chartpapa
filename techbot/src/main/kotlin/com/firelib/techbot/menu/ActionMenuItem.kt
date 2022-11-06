package com.firelib.techbot.menu

import com.firelib.techbot.MsgEnum
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.User

class ActionMenuItem(
    val name: MsgEnum,
    val action: suspend  ((bot: Bot, update: User) -> Unit)
) : BotMenu {

    override fun name(): MsgEnum {
        return name
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = action
    }
}