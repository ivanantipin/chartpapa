package com.firelib.transaq

import java.util.concurrent.LinkedBlockingQueue

class TrqContMsgListener<T : TrqMsg>(val predicate: (TrqMsg) -> Boolean){

    val queue = LinkedBlockingQueue<T>()

    fun process(msg : TrqMsg) {
        if(predicate(msg)){
            queue.offer(msg as T)
        }
    }


}