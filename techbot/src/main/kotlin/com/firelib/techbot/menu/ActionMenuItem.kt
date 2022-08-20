package com.firelib.techbot.menu

import com.firelib.techbot.Msg
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

class ActionMenuItem(
    val name: Msg,
    val action: ((bot: Bot, update: Update) -> Unit)
) : IMenu {

    override fun name(): Msg {
        return name
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = action
    }
}