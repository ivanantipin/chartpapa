package firelib.core.store.reader

import firelib.core.domain.Ohlc
import firelib.core.domain.Timed
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue

/**

 */
interface MarketDataReader<out T : Timed> : AutoCloseable{

    fun seek(time:Instant) : Boolean

    fun current(): T

    fun read(): Boolean

    fun startTime() : Instant

    fun endTime() : Instant

    fun isReadable() : Boolean{
        return true
    }
}

interface SimplifiedReader{

    fun peek() : Ohlc?

    fun poll() : Ohlc
}

fun SimplifiedReader.skipUntil(time : Instant){
    while (peek() != null && peek()!!.time() < time){
        poll()
    }
}

class QueueSimplifiedReader : SimplifiedReader{
    val queue = LinkedBlockingQueue<Ohlc>()

    override fun peek(): Ohlc? {
        return queue.peek()
    }

    override fun poll(): Ohlc {
        return queue.poll()
    }
}
