package com.firelib.techbot.menu

import com.firelib.techbot.MsgLocalazer
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

class ActionMenuItem(
    val name: MsgLocalazer,
    val action: ((bot: Bot, update: Update) -> Unit)
) : IMenu {

    override fun name(): MsgLocalazer {
        return name
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = action
    }
}