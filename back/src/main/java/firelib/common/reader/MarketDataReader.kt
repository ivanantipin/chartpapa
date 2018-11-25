package firelib.common.reader

import java.time.Instant

import firelib.domain.Timed

/**

 */
interface MarketDataReader<out T : Timed> : AutoCloseable{

    fun seek(time:Instant) : Boolean

    fun current(): T

    fun read(): Boolean

    fun startTime() : Instant

    fun endTime() : Instant
}