package com.firelib.techbot.menu

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

class ActionMenuItem(
    val name: String,
    val action: ((bot: Bot, update: Update) -> Unit)
) : IMenu {

    override fun name(): String {
        return name
    }

    override fun act(bot: Bot, update: Update) {
        action(bot, update)
    }

    override fun register(registry: MenuRegistry) {
    }

}