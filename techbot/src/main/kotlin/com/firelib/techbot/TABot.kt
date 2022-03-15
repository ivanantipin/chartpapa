package com.firelib.techbot

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future

val databaseExecutor = Executors.newSingleThreadExecutor()

val resultExecutor = Executors.newFixedThreadPool(10)


inline fun <R> measureTime(block: () -> R): Pair<R, Long> {
    val start = System.currentTimeMillis()
    val answer = block()
    return answer to System.currentTimeMillis() - start
}

inline fun <R> measureAndLogTime(msg: String, block: () -> R, minTimeMs : Long = 1000): Pair<R, Long> {
    try {
        val (r, l) = measureTime(block)
        if(minTimeMs < l){
            mainLogger.info("time spent on ${msg} is ${l / 1000.0} s.")
        }
        return r to l
    }catch (e : Exception){
        mainLogger.error("failed to run ${msg}")
        throw e
    }
}


val mainLogger = LoggerFactory.getLogger("main")


fun <T> updateDatabase(name: String, block: () -> T): CompletableFuture<T> {
    val f = databaseExecutor.submit(
        Callable<T> {
            transaction {
                addLogger(StdOutSqlLogger)
                val (value, duration) = measureTime {
                    block()
                }
                mainLogger.info("time spent on ${name} is ${duration / 1000.0} s.")
                value
            }
        }
    )
    return CompletableFuture.supplyAsync({f.get()}, resultExecutor)
}


object CommandsLog : IntIdTable() {
    val user = integer("user_id")
    val cmd = varchar("cmd", 300)
    val timestamp = long("timestamp_ms")
}