package com.firelib.techbot.menu

import com.firelib.techbot.Msg

interface IMenu{
    fun name() : Msg
    fun register(registry: MenuRegistry)
}