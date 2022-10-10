package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd

class SimpleButton(override val name: String, override val data: Cmd) : IButton {
    override fun children(): List<IButton> {
        return emptyList()
    }

    override fun register(menuRegistry: MenuRegistry) {
    }
}