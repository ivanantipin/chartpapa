package firelib.common.reader

import firelib.domain.Ohlc
import firelib.domain.Timed
import java.time.Instant

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

fun MarketDataReader<Ohlc>.pollOhlcsTill(time: Instant): Sequence<Ohlc> {
    return sequence({
        while (isReadable() && current().time() <= time) {
            yield(current())
            if (!read()) {
                break
            }
        }
    })
}
