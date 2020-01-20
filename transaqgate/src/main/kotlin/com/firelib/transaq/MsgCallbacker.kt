package com.firelib.transaq

import com.firelib.Empty
import com.firelib.TransaqConnectorGrpc
import com.google.common.util.concurrent.SettableFuture
import org.apache.commons.text.StringEscapeUtils
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor

class MsgCallbacker(val blockingStub: TransaqConnectorGrpc.TransaqConnectorBlockingStub, executor: Executor){

    val listeners = ConcurrentLinkedQueue<TrqCondMsgListener<out TrqMsg>>()

    val continuos = ConcurrentLinkedQueue<TrqContMsgListener<out TrqMsg>>()

    val plain = ConcurrentLinkedQueue<(TrqMsg)->Unit>()

    init {
        Thread {
            while (true) {
                try {
                    val messages = blockingStub.connect(Empty.newBuilder().build())
                    //continuous messages, this call will generally block till the end
                    messages.forEachRemaining {
                        val msg = TrqParser.parseTrqMsg(StringEscapeUtils.unescapeJava(it.txt))
                        if (msg != null) {
                            executor.execute {

                                listeners.removeIf {
                                    it.check(msg)
                                }
                                continuos.forEach {
                                    it.process(msg)
                                }

                                plain.forEach{callback->
                                    callback(msg)
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


    fun <T : TrqMsg> getNext(predicate : (TrqMsg)->Boolean) : SettableFuture<T> {
        val ret = SettableFuture.create<T>()
        listeners.add(TrqCondMsgListener<T>(predicate, ret))
        return ret
    }

    fun <T : TrqMsg> getContinuos(predicate : (TrqMsg)->Boolean) : TrqContMsgListener<T> {
        val ret = TrqContMsgListener<T>(predicate)
        continuos.add(ret)
        return ret
    }

    fun addPlainListener(listener: (TrqMsg)->Unit){
        plain.add(listener)
    }


}