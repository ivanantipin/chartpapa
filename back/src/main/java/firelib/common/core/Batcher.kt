package firelib.common.core

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class Batcher<T>(val batchProcessor : (List<T>)->Unit, val threadName : String) : Thread(threadName) {
    val queue = LinkedBlockingQueue<T>()

    init {
        isDaemon = true
    }


    fun add(t : T){
        queue.add(t)
    }

    override fun run(){
        val buffer = mutableListOf<T>()
        while (!isInterrupted || queue.isNotEmpty()){
            try {
                buffer += queue.poll(100, TimeUnit.DAYS)
                processBuffer(buffer)
            }catch (e : RuntimeException){
                println(" ${threadName} : runtime exception in happened ${e}")
            } catch (e : InterruptedException){
                println("processing finish buffer")
                processBuffer(buffer)
                break;
            }
        }
    }

    private fun processBuffer(buffer: MutableList<T>) {
        queue.drainTo(buffer)
        if (buffer.isNotEmpty()) {
            println("processing ${buffer}")
            batchProcessor(buffer)
            buffer.clear()
        }
    }

    fun cancelAndJoin(){
        interrupt()
        join()
    }

    fun addAll(batch: List<T>) {
        queue += batch
    }
}