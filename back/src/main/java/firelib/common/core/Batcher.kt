package firelib.common.core

import java.util.concurrent.LinkedBlockingQueue

class Batcher<T>(val batchProcessor : (List<T>)->Unit, val threadName : String) : Thread(threadName) {
    val queue = LinkedBlockingQueue<T>()


    fun add(t : T){
        queue.add(t)
    }

    override fun run(){
        val buffer = mutableListOf<T>()
        while (!isInterrupted){
            try {
                buffer += queue.poll()
                queue.drainTo(buffer)
                batchProcessor(buffer)
                buffer.clear()
            }catch (e : RuntimeException){
                println(" ${threadName} : runtime exception in happened ${e}")
            } catch (e : InterruptedException){
                break;
            }
        }
    }

    fun cancelAndJoin(){
        interrupt()
        join()
    }

    fun addAll(addTrade: List<T>) {
        queue += addTrade
    }
}