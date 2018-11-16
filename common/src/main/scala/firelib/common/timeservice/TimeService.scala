package firelib.common.timeservice

import java.time.Instant

trait TimeService{
    def currentTime :Instant
}
