package com.firelib.stratserver

import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class Brodcaster<T>(p0: String, val maxSize: Int = 30) : Thread(p0) {

    val observersQueue = LinkedBlockingQueue<StreamObserver<T>>()
    val queue = LinkedBlockingQueue<T>()

    fun add(t: T) {
        queue += t
    }


    fun addObserver(t: StreamObserver<T>) {
        observersQueue += t
    }

    override fun run() {
        val history = LinkedList<T>()
        val observers = mutableListOf<StreamObserver<T>>()
        while (true) {
            try {
                val poll = queue.poll(1, TimeUnit.SECONDS)
                if (poll != null) {
                    history += poll
                    if (history.size > maxSize) {
                        history.removeFirst()
                    }
                    val removed = mutableListOf<StreamObserver<T>>();
                    observers.forEach {
                        try {
                            it.onNext(poll)
                        } catch (e: StatusRuntimeException) {
                            print("status runtime ${e} happend on ${it} removing")
                            removed += it;
                        }
                    }
                    observers -= removed
                }
                val obs = observersQueue.poll(1, TimeUnit.MILLISECONDS)
                if (obs != null) {
                    history.forEach({
                        obs.onNext(it)
                    })
                    observers += obs
                }
            } catch (e: Exception) {
                print("unexpected exception ${e}")
            }
        }
    }
}