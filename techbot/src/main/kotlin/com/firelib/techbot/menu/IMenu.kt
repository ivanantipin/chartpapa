package com.firelib.techbot.menu

import com.firelib.techbot.MsgLocalizer

interface IMenu {
    fun name(): MsgLocalizer
    fun register(registry: MenuRegistry)
}