package com.firelib.techbot.menu

import com.firelib.techbot.MsgLocalizer
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

class ActionMenuItem(
    val name: MsgLocalizer,
    val action: ((bot: Bot, update: Update) -> Unit)
) : IMenu {

    override fun name(): MsgLocalizer {
        return name
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = action
    }
}