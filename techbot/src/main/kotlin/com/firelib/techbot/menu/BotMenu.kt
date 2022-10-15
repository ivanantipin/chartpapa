package com.firelib.techbot.menu

import com.firelib.techbot.MsgLocalizer

interface BotMenu {
    fun name(): MsgLocalizer
    fun register(registry: MenuRegistry)
}