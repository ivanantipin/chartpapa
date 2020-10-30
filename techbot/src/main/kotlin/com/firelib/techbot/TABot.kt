package com.firelib.techbot

import com.firelib.techbot.command.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TABot {

    val map = mutableMapOf<String, CommandHandler>()

    init {
        register(TrendsCommand())
        register(TickersListHandler())
        register(SubHandler())
        register(RmHandler())
        register(DemarkCommand())
        register(LevelsCommand())
        register(HelpListHandler(this))
    }

    fun register(handler: CommandHandler) {
        handler.commands().forEach {
            map[it] = handler
        }
    }

    fun parseCmd(cmd: String): Command {
        val arr = cmd.split("\\s".toRegex())
        return Command(arr[0], arr.subList(1, arr.size))
    }

    fun handle(cmd: String, bot: Bot, update: Update) {
        GlobalScope.launch { // launch a new coroutine in background and continue
            val parsed = parseCmd(cmd)
            val handler = map.get(parsed.cmd)
            if (handler != null) {
                handler.handle(parsed, bot, update)
            } else {
                UnknownCmdHandler(this@TABot).handle(parsed, bot, update)

            }
        }
    }
}