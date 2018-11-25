package firelib.common.timeservice

import java.time.Instant

class TimeServiceReal : TimeService{
    override fun currentTime(): Instant = Instant.now()
}