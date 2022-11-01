package firelib.core.misc

import firelib.core.domain.Interval
import kotlinx.coroutines.delay
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

suspend fun waitUntilSuspend(timestamp: Instant) {
    val millis = timestamp.toEpochMilli() - System.currentTimeMillis()
    if (millis <= 0) return
    try {
        delay(millis)
    } catch (e: InterruptedException) {
        throw RuntimeException(e.message, e)
    }
}

suspend fun timeSequence(startTime: Instant, interval: Interval, msShift : Long = 1000, callback : suspend (Instant)->Unit) {
    var time = interval.roundTime(startTime)
    while (true) {
        callback(time)
        time += interval.duration
        waitUntilSuspend(time.plusMillis(msShift)) // wait a bit for md to arrive
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

suspend fun main() {
    timeSequence(Instant.now(), Interval.Sec1, 0, {println(it)})

}

