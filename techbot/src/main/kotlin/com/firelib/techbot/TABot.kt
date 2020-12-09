package com.firelib.techbot

import com.firelib.techbot.command.CommandHandler
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future


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
        }
    }

}


object CommandsLog : IntIdTable() {
    val user = integer("user_id")
    val cmd = varchar("cmd", 300)
    val timestamp = long("timestamp_ms")
}