package firelib.common.reader

import java.time.Instant

import firelib.domain.Timed

/**

 */
trait MarketDataReader[+T <: Timed] extends AutoCloseable{

    def seek(time:Instant) : Boolean

    def current: T

    def read(): Boolean

    def startTime() : Instant

    def endTime() : Instant
}
