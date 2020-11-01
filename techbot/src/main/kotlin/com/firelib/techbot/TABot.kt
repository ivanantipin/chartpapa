package com.firelib.techbot

import com.firelib.techbot.command.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class TABot {

    val map = mutableMapOf<String, CommandHandler>()

    init {
        register(TrendsCommand())
        register(TickersListHandler())
        register(SubHandler())
        register(RmHandler())
        register(DemarkCommand())
        register(LevelsCommand())
        register(StartHandler())
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
            saveCmd(cmd, bot, update)
            val parsed = parseCmd(cmd)
            val handler = map.get(parsed.cmd)
            if (handler != null) {
                handler.handle(parsed, bot, update)
            } else {
                UnknownCmdHandler(this@TABot).handle(parsed, bot, update)
            }
        }
    }

    fun saveCmd(cmd: String, bot: Bot, update: Update){
        try {
            val user = update.message!!.from!!
            transaction {
                BotHelper.ensureExist(user)
                CommandsLog.insert {
                    it[CommandsLog.user] = user.id.toInt()
                    it[CommandsLog.cmd] = cmd
                    it[CommandsLog.timestamp] = System.currentTimeMillis()
                }
            }
        } catch (e: Exception) {
            println("failed to save ")
        }
    }
}


object CommandsLog : IntIdTable() {
    val user = integer("user_id")
    val cmd = varchar("cmd", 300)
    val timestamp = long("timestamp_ms")
}