package com.firelib.techbot

import com.firelib.techbot.InflightHandler.registerInflight
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

object InflightHandler {

    val inflightRequests = ConcurrentHashMap<String,ConcurrentHashMap<Long,Long>>()

    val log = LoggerFactory.getLogger(javaClass)

    val counter = AtomicLong()

    suspend fun <T> registerInflight(category : String, block : suspend ()->T) : T{
        val coll =
            inflightRequests.computeIfAbsent(category, { ConcurrentHashMap()})
        val id = counter.incrementAndGet()
        coll[id] =  System.currentTimeMillis()
        try {
            return block()
        }finally {
            coll.remove(id)
        }

    }


    fun start(){
        thread {
            while (true){
                inflightRequests.forEach{ (cat, requests) ->
                    log.info("number of inflight for category ${cat} is ${requests.size}")
                    val cnt = requests.asSequence().filter { System.currentTimeMillis() - it.value > 10_000 }.count()
                    log.info("number of long running inflight for category ${cat} is ${cnt}")
                }
                Thread.sleep(10_000L)
            }

        }


    }


}



