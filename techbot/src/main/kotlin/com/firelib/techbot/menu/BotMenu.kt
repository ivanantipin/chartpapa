package com.firelib.techbot.menu

import com.firelib.techbot.MsgEnum

interface BotMenu {
    fun name(): MsgEnum
    fun register(registry: MenuRegistry)
}