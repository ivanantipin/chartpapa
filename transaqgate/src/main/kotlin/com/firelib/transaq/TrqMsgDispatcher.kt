package com.firelib.transaq

import com.firelib.Empty
import com.firelib.TransaqConnectorGrpc
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

val log = LoggerFactory.getLogger(TrqMsgDispatcher::class.java)

class TrqMsgDispatcher(val stub: TransaqConnectorGrpc.TransaqConnectorBlockingStub){

    val listeners = ConcurrentLinkedQueue<Receiver<Any>>()



    init {
        Thread {
            while (true) {
                try {
                    val messages = stub.connect(Empty.newBuilder().build())
                    //continuous messages, this call will generally block till the end
                    messages.forEachRemaining {
                        val msg = TrqParser.parseTrqMsg(StringEscapeUtils.unescapeJava(it.txt))
                        if (msg != null) {
                            listeners.forEach {
                                if(!it.isCancelled.get()){
                                    if(it.predicate(msg)){
                                        it.pub(msg)
                                    }
                                }else{
                                    listeners.remove(it)
                                }
                            }
                        }


                    }
                } catch (e: Exception) {
                    println("error processing ${e}")
                    Thread.sleep(10_000)
                }
            }
        }.start()
    }

    fun <T : TrqMsg> add(predicate : (TrqMsg)->Boolean) : QueueReceiver<T>{
        val ret = QueueReceiver<T>(predicate)
        listeners.add(ret as QueueReceiver<Any>)
        return ret
    }
    fun <T : TrqMsg> addSync(predicate : (TrqMsg)->Boolean, consumer : (T)->Unit) : SyncReceiver<T>{
        val ret = SyncReceiver<T>(consumer, predicate)
        listeners.add(ret as SyncReceiver<Any>)
        return ret
    }

}

abstract class Receiver<T>(val predicate : (TrqMsg)->Boolean) : AutoCloseable{
    val isCancelled = AtomicBoolean(false)

    override fun close() {
        isCancelled.set(true)
    }

    abstract fun pub(msg : T)

}

class SyncReceiver<T>(val consumer : (T)->Unit,  predicate : (TrqMsg)->Boolean) : Receiver<T>(predicate) {
    override fun pub(msg: T) {
        try{
            consumer(msg)
        }catch (e : Exception){
            log.error("error publishng to consumer ", e)
        }

    }
}

class QueueReceiver<T>( predicate : (TrqMsg)->Boolean) : Receiver<T>(predicate) {
    val queue =  LinkedBlockingQueue<T>()
    override fun pub(msg: T) {
        queue.offer(msg)
    }

}