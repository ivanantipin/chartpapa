package com.firelib.techbot.menu

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

interface IMenu{
    fun name() : String
    fun act(bot: Bot, update: Update)
    fun register(registry: MenuRegistry)
}