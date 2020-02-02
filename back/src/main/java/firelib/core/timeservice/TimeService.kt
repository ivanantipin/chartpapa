package firelib.core.timeservice

import firelib.core.misc.atUtc
import java.time.Instant
import java.time.LocalDateTime

interface TimeService{
    fun currentTime ():Instant

    fun currentTimeUtc ():LocalDateTime{
        return currentTime().atUtc()
    }
}