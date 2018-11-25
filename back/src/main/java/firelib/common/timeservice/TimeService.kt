package firelib.common.timeservice

import java.time.Instant

interface TimeService{
    fun currentTime ():Instant
}