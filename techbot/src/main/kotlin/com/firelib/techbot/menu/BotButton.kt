package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd

interface BotButton {
    val name: String
    val data: Cmd
    fun children(): List<BotButton>
    fun register(menuRegistry: MenuRegistry)
}