package com.firelib.techbot

import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

val databaseExecutor = Executors.newSingleThreadExecutor(ThreadFactory {
    Thread(it).apply {
        name = "dbExecutor"
    }
})

val resultExecutor = Executors.newFixedThreadPool(10)

inline fun <R> measureTime(block: () -> R): Pair<R, Long> {
    val start = System.currentTimeMillis()
    val answer = block()
    return answer to System.currentTimeMillis() - start
}

inline fun <R> measureAndLogTime(msg: String, block: () -> R, minTimeMs: Long = 1000): Pair<R, Long> {
    try {
        val (r, l) = measureTime(block)
        if (minTimeMs < l) {
            mainLogger.info("time spent on ${msg} is ${l / 1000.0} s.")
        }
        return r to l
    } catch (e: Exception) {
        mainLogger.error("failed to run ${msg}")
        throw e
    }
}

val mainLogger = LoggerFactory.getLogger("main")

fun dumpThreads() {
    val threadMxBean = ManagementFactory.getThreadMXBean()

    for (ti in threadMxBean.dumpAllThreads(true, true)) {
        print(ti.toString())
    }
}

fun <T> updateDatabase(name: String, block: () -> T): CompletableFuture<T> {

    fun rrun(): T {
        return transaction {
            try {
                //addLogger(StdOutSqlLogger)
                val (value, duration) = measureTime {
                    block()
                }
                mainLogger.info("time spent on ${name} is ${duration / 1000.0} s.")
                value
            } catch (e: Exception) {
                dumpThreads()
                throw e
            }
        }
    }

    return if (Thread.currentThread().name == "dbExecutor") {
        mainLogger.info("executing ${name} without submitting")
        CompletableFuture.completedFuture(rrun())
    } else {
        val f = databaseExecutor.submit(
            Callable<T> {
                rrun()
            }

        )
        CompletableFuture.supplyAsync({ f.get() }, resultExecutor)
    }
}


