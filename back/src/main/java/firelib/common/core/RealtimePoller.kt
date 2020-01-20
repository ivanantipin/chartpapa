package firelib.common.core

import com.funstat.domain.InstrId
import firelib.common.interval.Interval
import firelib.common.misc.atUtc
import firelib.domain.Ohlc
import java.time.Duration
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue

class RealtimePoller(val instrs: InstrId,
                     val source: Source,
                     val startTime: Instant,
                     val interval : Interval,
                     val queue: LinkedBlockingQueue<Ohlc>) {

    val thread = Thread {
        var stT = startTime
        while (true) {
            var nonEmpty = false
            source.load(instrs, stT.atUtc()).forEach { oh ->
                if (oh.endTime > stT) {
                    queue.offer(oh)
                    stT = oh.endTime
                    nonEmpty = true
                }
            }

            if(!nonEmpty){
                println("no data , sleeping ${Instant.now()}")
                Thread.sleep(interval.durationMs/2)
            }
            val timestamp = stT.plusMillis(interval.durationMs/2)
            waitUntil(timestamp)
        }
    }
}

