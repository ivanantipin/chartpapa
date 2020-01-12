package com.firelib.stratserver

import io.grpc.stub.StreamObserver
import java.util.*

class Broadcaster<T>(p0: String, val maxSize: Int = 1, val historyKey : (T)->String ={"dummy"}) {

    val distributor = ObserverDistributor<T>()
    val history = mutableMapOf<String,LinkedList<T>>()

    @Synchronized
    fun add(t: T) {
        val key = historyKey(t)
        val hist = history.computeIfAbsent(key,{LinkedList()})
        hist  += t
        if (hist.size > maxSize) {
            hist.removeFirst()
        }
        distributor.distribute(t)
    }


    @Synchronized
    fun addObserver(obs: StreamObserver<T>) {
        history.values.flatMap { it }.forEach{
            obs.onNext(it)
        }
        distributor.addObserver(obs)
    }

}

