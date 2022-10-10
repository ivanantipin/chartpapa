package com.firelib.techbot.menu

import com.firelib.techbot.MsgLocalazer

interface IMenu{
    fun name() : MsgLocalazer
    fun register(registry: MenuRegistry)
}