package com.firelib.transaq

import firelib.core.InstrumentMapper
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.atMoscow
import firelib.core.misc.timeSequence
import firelib.core.store.ReaderFactory
import firelib.core.store.reader.QueueSimplifiedReader
import firelib.core.store.reader.SimplifiedReader
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class TrqRealtimeReaderFactory(
    val dist: TrqMsgDispatcher,
    val interval: Interval,
    val instrumentMapper: InstrumentMapper
) : ReaderFactory {

    val log = LoggerFactory.getLogger(javaClass)

    val intervalListeners =
        ConcurrentLinkedQueue<(Instant) -> Unit>()


    val securities = ConcurrentHashMap<String, String>()

    fun subscribe(sec: String) {
        try {
            val instrId = instrumentMapper(sec)!!
            securities[sec] = sec
            dist.stub.command(TrqCommandHelper.subscribe(instrId.code, instrId.board))
        } catch (e: Exception) {
            log.error("exception subscribing to ${sec}", e)
        }
    }

    fun resubscribe() {
        securities.forEach({ subscribe(it.key) })
    }

    val lastTickReceived = AtomicLong(0)

    init {
        Thread {
            timeSequence(
                Instant.now(),
                interval,
                100
            ).forEach { time ->
                intervalListeners.forEach {
                    try {
                        it(time)
                    } catch (e: Exception) {
                        log.info("failed to update listener", e)
                    }

                }
            }
        }.start()

        dist.addSync<AllTrades>({ it is AllTrades }, { lastTickReceived.set(System.currentTimeMillis()) })

        val serverStatus = dist.add<ServerStatus> { it is ServerStatus }

        Thread {
            while (true) {
                val status = serverStatus.queue.poll(1, TimeUnit.MINUTES)
                if ("true" == status?.connected
                    && (System.currentTimeMillis() - lastTickReceived.get()) > 2 * 60_000
                    && Instant.now().atMoscow().toLocalTime() >= LocalTime.of(10,0)
                    && Instant.now().atMoscow().toLocalTime() < LocalTime.of(19,0) // fixme would not work for other markets
                ) {
                    log.info("resubscribing for ${securities}")
                    resubscribe()
                }
            }
        }.start()
    }

    fun map(security: String): InstrId {
        return instrumentMapper(security)!!
    }

    val stat = ConcurrentHashMap<String, AtomicLong>()

    var lastTime = System.currentTimeMillis()

    override fun makeReader(security: String): SimplifiedReader {
        log.info("creating reader for security ${security}")
        val instrId = map(security)
        val ret = QueueSimplifiedReader()
        val receiver = dist.add<AllTrades> { it is AllTrades }

        intervalListeners.add {
            val lst = mutableListOf<AllTrades>()
            receiver.queue.drainTo(lst)
            val ticks = lst.flatMap { it.ticks }.filter { it.seccode == instrId.code && it.board == instrId.board }

            val cnt = stat.computeIfAbsent(security) { AtomicLong(0) }
            cnt.addAndGet(ticks.size.toLong())

            if (System.currentTimeMillis() - lastTime > 60 * 60_000  ) {
                log.info("receidev ticks last hour ${stat}")
                stat.clear()
                lastTime = System.currentTimeMillis()
            }

            if (ticks.isNotEmpty()) {
                ret.queue.offer(
                    Ohlc(
                        endTime = it,
                        open = ticks.first().price,
                        high = ticks.maxBy { it.price }!!.price,
                        low = ticks.minBy { it.price }!!.price,
                        close = ticks.last().price,
                        volume = ticks.sumBy { it.quantity }.toLong(),
                        interpolated = false

                    )
                )
            }
        }
        subscribe(security)
        return ret
    }
}
