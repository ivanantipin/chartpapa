package firelib.emulator

import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.timeSequence
import firelib.core.store.reader.QueueSimplifiedReader
import firelib.core.store.reader.SimplifiedReader
import java.time.Instant

class RtReaderEmulator(val interval : Interval) : SimplifiedReader {

    val delegate = QueueSimplifiedReader()

    init {
        val historicalSourceEmulator = HistoricalSourceEmulator()
        Thread{
            val start = interval.roundTime(Instant.now())
            timeSequence(start, interval, 0).forEach {
                delegate.queue.offer(historicalSourceEmulator.makeRandomOhlc(it))
            }
        }.start()
    }
    override fun peek(): Ohlc? {
        return delegate.peek()
    }

    override fun poll(): Ohlc {
        return delegate.poll()
    }
}