package com.firelib.transaq

import com.google.common.util.concurrent.SettableFuture

class TrqCondMsgListener<T : TrqMsg>(val predicate: (TrqMsg) -> Boolean, val future : SettableFuture<T>){

    fun check(msg : TrqMsg) : Boolean{
        val ret = predicate(msg)
        if(ret){
            future.set(msg as T)
        }
        return ret
    }
}