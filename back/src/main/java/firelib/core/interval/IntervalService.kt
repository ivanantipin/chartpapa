package firelib.core.interval

import firelib.core.domain.Interval
import java.time.Instant

/**
 * interval service to check intervals end and send notification to listeners
 */
interface IntervalService  {
    fun addListener(interval: Interval, action: (Instant) -> Unit)
}