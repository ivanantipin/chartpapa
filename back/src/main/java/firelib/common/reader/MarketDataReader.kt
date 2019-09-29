package firelib.common.reader

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
}

