package com.firelib.transaq

import com.firelib.TransaqConnectorGrpc
import firelib.core.domain.InstrId
import firelib.core.InstrumentMapper
import firelib.core.store.ReaderFactory
import firelib.core.misc.timeSequence
import firelib.core.domain.Interval
import firelib.core.store.reader.QueueSimplifiedReader
import firelib.core.store.reader.SimplifiedReader
import firelib.core.domain.Ohlc
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class TrqRealtimeReaderFactory(
    val stub: TransaqConnectorGrpc.TransaqConnectorBlockingStub,
    val interval: Interval,
    val instrumentMapper: InstrumentMapper
) :
    ReaderFactory {

    val log = LoggerFactory.getLogger(javaClass)

    val dist = MsgCallbacker(stub)

    val ticks =
        ConcurrentLinkedQueue<(Instant) -> Unit>()


    val securities = ConcurrentHashMap<String, String>()

    fun subscribe(sec: String) {
        try {
            val instrId = instrumentMapper(sec)!!
            securities[sec] = sec
            stub.command(TrqCommandHelper.subscribe(instrId.code, instrId.board))
        } catch (e: Exception) {
            log.error("exception subscribing to ${sec}", e)
        }
    }

    fun resubscribe() {
        securities.forEach({ subscribe(it.key) })
    }

    init {
        Thread {
            timeSequence(
                Instant.now(),
                interval,
                100
            ).forEach { time ->
                ticks.forEach { it(time) }
            }
        }.start()

        val serverStatus = dist.add<ServerStatus> { it is ServerStatus }

        Thread {
            while (true) {
                val status = serverStatus.queue.poll(1, TimeUnit.MINUTES)
                if ("true" == status?.connected) {
                    log.info("resubscribing for ${securities}")
                    resubscribe()
                }
            }
        }.start()
    }


    fun map(security: String): InstrId {
        return instrumentMapper(security)
    }

    override fun makeReader(security: String): SimplifiedReader {
        val instrId = map(security)
        val ret = QueueSimplifiedReader()
        val receiver = dist.add<AllTrades> { it is AllTrades }

        ticks.add {
            val lst = mutableListOf<AllTrades>()
            receiver.queue.drainTo(lst)
            val ticks = lst.flatMap { it.ticks }.filter { it.seccode == instrId.code && it.board == instrId.board }
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
