package com.firelib.stratserver

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class Defferer{

    private val schedulers = ConcurrentHashMap<String,ScheduledFuture<*>>();

    val executors = Executors.newScheduledThreadPool(1)

    fun executeLater(name : String, action : ()->Unit){
        schedulers.get(name)?.cancel(false)
        schedulers[name] = executors.schedule(action, 300, TimeUnit.MILLISECONDS)
    }

    fun executeLater(action : ()->Unit){
        executeLater("dummy", action)
    }
}