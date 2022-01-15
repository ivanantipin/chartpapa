package com.firelib.techbot.menu

interface IMenu{
    fun name() : String
    fun register(registry: MenuRegistry)
}