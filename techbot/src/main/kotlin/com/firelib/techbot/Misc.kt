package com.firelib.techbot

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import java.lang.management.ManagementFactory

object Misc{

    suspend fun <T> Flow<T>.batchedCollect(size : Int = 1000, callback : suspend (List<T>)->Unit){
        val buffer = mutableListOf<T>()

        this.buffer(2000).collect{
            buffer.add(it)
            if(buffer.size >= size){
                callback(buffer)
                buffer.clear()
            }
        }
        if(buffer.isNotEmpty()){
            callback(buffer)
        }
    }


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