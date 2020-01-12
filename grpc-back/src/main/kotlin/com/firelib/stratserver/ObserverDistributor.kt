package com.firelib.stratserver

import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import java.util.concurrent.ConcurrentLinkedQueue

class ObserverDistributor<T>{

    val list = ConcurrentLinkedQueue<StreamObserver<T>>()

    fun addObserver(observer : StreamObserver<T>){
        list += observer;
    }

    fun distribute(value : T){
        list.forEach {
            try {
                it.onNext(value)
            } catch (e: StatusRuntimeException) {
                print("status runtime ${e} happend on ${it} removing")
                list -= it
            }
        }
    }
}