package firelib.common.timeservice

import java.time.Instant

class TimeServiceReal extends TimeService{
    override def currentTime: Instant = Instant.now()
}
