package com.firelib.techbot

import com.github.kotlintelegrambot.entities.ParseMode

fun main() {
    val bot = makeBot(TABot())
    val msg = "[inline URL](https://teletype.in/@techbot/3lSiYgnpG)"
    bot.sendMessage(312778820, msg, parseMode = ParseMode.MARKDOWN)
}