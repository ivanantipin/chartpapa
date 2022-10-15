package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd

class SimpleButton(override val name: String, override val data: Cmd) : BotButton {
    override fun children(): List<BotButton> {
        return emptyList()
    }

    override fun register(menuRegistry: MenuRegistry) {
    }
}