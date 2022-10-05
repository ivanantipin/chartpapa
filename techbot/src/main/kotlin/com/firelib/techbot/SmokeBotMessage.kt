package com.firelib.techbot

import com.firelib.techbot.command.SettingsCommand
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.logging.LogLevel

fun main() {

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        mainLogger.error("unhandled exception thrown", throwable)
    }

    val bot = bot {
        token = ConfigParameters.TELEGRAM_TOKEN.get()!!
        timeout = 30
        logLevel = LogLevel.Network.Basic
        dispatch {
            text(null) {
                try {
                } catch (e: Exception) {
                    mainLogger.error("exception in action ${text}", e)
                }
            }
            callbackQuery(null) {
                try {

                } catch (e: Exception) {
                    mainLogger.error("exception in call back query ${this.callbackQuery?.data}")
                }
            }
        }

    }

    initDatabase()

    SettingsCommand.displaySettings(bot, 312778820L)
}


