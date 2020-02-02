package firelib.core

import firelib.core.domain.Interval
import java.time.Instant

fun waitUntil(timestamp: Instant) {
    val millis = timestamp.toEpochMilli() - System.currentTimeMillis()
    if (millis <= 0) return
    try {
        Thread.sleep(millis)
    } catch (e: InterruptedException) {
        throw RuntimeException(e.message, e)
    }
}

fun timeSequence(startTime: Instant, interval: Interval, msShift : Long = 1000): Sequence<Instant> {
    var time = interval.roundTime(startTime)
    return sequence {
        while (true) {
            yield(time)
            time += interval.duration
            waitUntil(time.plusMillis(msShift)) // wait a bit for md to arrive
        }
    }
}