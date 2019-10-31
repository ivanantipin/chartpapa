package com.firelib.stratserver

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class Defferer{
    private var schedule: ScheduledFuture<*>? = null

    val executors = Executors.newScheduledThreadPool(1)

    fun executeLater(action : ()->Unit){
        if(schedule != null){
            this.schedule!!.cancel(false)
        }
        this.schedule = executors.schedule(action, 300, TimeUnit.MILLISECONDS)
    }
}