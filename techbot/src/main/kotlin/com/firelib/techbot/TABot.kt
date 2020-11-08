package com.firelib.techbot

import com.firelib.techbot.command.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.logTimeSpent
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue


val databaseExecutor = Executors.newSingleThreadExecutor()


inline fun <R> measureTime(block: () -> R): Pair<R, Long> {
    val start = System.currentTimeMillis()
    val answer = block()
    return answer to System.currentTimeMillis() - start
}

inline fun <R> measureAndLogTime(msg: String, block: () -> R): Pair<R, Long> {
    val (r, l) = measureTime(block)
    mainLogger.info("time spent on ${msg} is ${l / 1000.0} s.")
    return r to l
}


val mainLogger = LoggerFactory.getLogger("main")


fun <T> updateDatabase(name: String, block: () -> T): Future<T> {

    return databaseExecutor.submit(
        Callable<T> {
            transaction {
                addLogger(StdOutSqlLogger)
                val (value, duration) = measureTime {
                    block()
                }
                mainLogger.info("time spent on ${name} is ${duration / 1000.0} s.")
                value
            }
        },
    )
}

class TABot {

    val map = mutableMapOf<String, CommandHandler>()

    val executors = Executors.newFixedThreadPool(20)

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
        map[handler.command()] = handler
    }

    fun parseCmd(cmd: String): Command {
        val arr = cmd.split("\\s".toRegex())
        return Command(arr[0], arr.subList(1, arr.size))
    }

    fun handle(cmd: String, bot: Bot, update: Update) {
        executors.execute {

            updateDatabase("save command") {
                val user = update.message!!.from!!
                BotHelper.ensureExist(user)
                CommandsLog.insert {
                    it[CommandsLog.user] = user.id.toInt()
                    it[CommandsLog.cmd] = cmd
                    it[timestamp] = System.currentTimeMillis()
                }
            }

            val parsed = parseCmd(cmd)
            val handler = map.get(parsed.cmd)
            if (handler != null) {
                measureAndLogTime("processing command ${handler::class}") {
                    handler.handle(parsed, bot, update)
                }
            } else {
                UnknownCmdHandler(this@TABot).handle(parsed, bot, update)
            }
        }
    }

}


object CommandsLog : IntIdTable() {
    val user = integer("user_id")
    val cmd = varchar("cmd", 300)
    val timestamp = long("timestamp_ms")
}