package firelib.tcs

import firelib.core.timeSequence
import firelib.core.domain.Interval
import firelib.common.reader.QueueSimplifiedReader
import firelib.common.reader.SimplifiedReader
import firelib.core.domain.Ohlc
import java.time.Instant

class RtReaderEmulator(val interval : Interval) : SimplifiedReader {

    val delegate = QueueSimplifiedReader()

    init {
        val historicalSourceEmulator = HistoricalSourceEmulator(interval)
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