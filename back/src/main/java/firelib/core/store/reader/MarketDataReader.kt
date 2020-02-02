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

class SimplifiedReaderAdapter(val mdReader : MarketDataReader<Ohlc>) : SimplifiedReader{
    override fun peek(): Ohlc {
        return mdReader.current()
    }

    override fun poll(): Ohlc {
        mdReader.read()
        return mdReader.current()
    }

}

fun MarketDataReader<Ohlc>.toSequence(): SimplifiedReader {
    return SimplifiedReaderAdapter(this)
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
