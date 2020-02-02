package firelib.tcs

import firelib.common.core.timeSequence
import firelib.domain.Interval
import firelib.common.reader.QueueSimplifiedReader
import firelib.common.reader.SimplifiedReader
import firelib.domain.Ohlc
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