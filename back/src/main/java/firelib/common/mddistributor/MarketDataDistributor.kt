package firelib.common.mddistributor

import firelib.common.interval.Interval
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**

 */
interface MarketDataDistributor {
    fun getOrCreateTs(tickerId: Int, interval: Interval, len: Int): TimeSeries<Ohlc>
    fun price(idx: Int): Ohlc
    fun addListener(interval: Interval, action: (Instant, MarketDataDistributor) -> Unit)
}

fun sequence(interval: Interval, ff : ()->Unit){
    val pool = Executors.newScheduledThreadPool(1)

    val next = interval.roundTime(Instant.now() + interval.duration)

    println(next)
    println(Instant.now())

    //next.toEpochMilli()

    val delay = next.toEpochMilli() - System.currentTimeMillis()


    pool.scheduleAtFixedRate({ff.invoke()}, delay,  interval.durationMs, TimeUnit.MILLISECONDS)


}

fun main(){
    sequence(Interval.Sec1, {
        println(Instant.now())
        println(Interval.Sec1.roundTime(Instant.now()))

        Thread.sleep(Random.nextLong (900))
    })
}