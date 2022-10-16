package com.firelib.techbot

import java.lang.management.ManagementFactory

object Misc{
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

    fun dumpThreads() {
        val threadMxBean = ManagementFactory.getThreadMXBean()

        for (ti in threadMxBean.dumpAllThreads(true, true)) {
            print(ti.toString())
        }
    }
}