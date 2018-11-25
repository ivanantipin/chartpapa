package firelib.domain

import java.time.Instant

interface Timed {
    fun time(): Instant
}