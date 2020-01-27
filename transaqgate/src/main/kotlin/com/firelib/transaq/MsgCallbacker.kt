package com.firelib.transaq

import com.firelib.Empty
import com.firelib.TransaqConnectorGrpc
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.apache.commons.text.StringEscapeUtils
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class MsgCallbacker(val blockingStub: TransaqConnectorGrpc.TransaqConnectorBlockingStub){

    val listeners = ConcurrentLinkedQueue<Receiver<Any>>()

    init {
        Thread {
            while (true) {
                try {
                    val messages = blockingStub.connect(Empty.newBuilder().build())
                    //continuous messages, this call will generally block till the end
                    messages.forEachRemaining {
                        val msg = TrqParser.parseTrqMsg(StringEscapeUtils.unescapeJava(it.txt))
                        if (msg != null) {
                            listeners.forEach {
                                if(!it.isCancelled.get()){
                                    if(it.predicate(msg)){
                                        it.queue.offer(msg)
                                    }
                                }else{
                                    listeners.remove(it)
                                }
                            }
                        }


                    }
                } catch (e: Exception) {
                    println("error processing ${e}")

                }
            }
        }.start()
    }

    fun <T : TrqMsg> add(predicate : (TrqMsg)->Boolean) : Receiver<T>{
        val ret = Receiver<T>(predicate)
        listeners.add(ret as Receiver<Any>)
        return ret
    }

}

class Receiver<T>(val predicate : (TrqMsg)->Boolean) : AutoCloseable{
    val isCancelled = AtomicBoolean(false)
    val queue =  LinkedBlockingQueue<T>()
    override fun close() {
        isCancelled.set(true)
    }
}