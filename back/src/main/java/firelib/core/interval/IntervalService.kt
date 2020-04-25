package firelib.core.interval

import firelib.core.domain.Interval
import java.time.Instant

/**
 * interval service to check intervals end and send notification to listeners
 */

typealias IServiceAction = (Instant) -> Unit

interface IntervalService  {
    fun addListener(interval: Interval, action: IServiceAction)
}