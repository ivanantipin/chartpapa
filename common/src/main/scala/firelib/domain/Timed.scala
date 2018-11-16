package firelib.domain

import java.time.Instant

trait Timed {
    def time: Instant
}
