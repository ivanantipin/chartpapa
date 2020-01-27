package com.firelib.transaq

import com.firelib.TransaqConnectorGrpc
import com.funstat.domain.InstrId
import firelib.common.core.InstrumentMapper
import firelib.common.core.ReaderFactory
import firelib.common.core.timeSequence
import firelib.common.interval.Interval
import firelib.common.reader.SimplifiedReader
import firelib.domain.Ohlc
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

class TrqRealtimeReaderFactory(val stub : TransaqConnectorGrpc.TransaqConnectorBlockingStub, val interval: Interval, val instrumentMapper: InstrumentMapper) :
    ReaderFactory {

    val dist = MsgCallbacker(stub)

    val concurrentQueue =
        ConcurrentLinkedQueue<(Instant) -> Unit>()

    init {
        Thread{
            timeSequence(
                Instant.now(),
                interval,
                100
            ).forEach { time->
                concurrentQueue.forEach {it(time)}
            }
        }.start()

    }

    fun map(security: String) : InstrId {
        return instrumentMapper(security)
    }

    override fun makeReader(security: String): SimplifiedReader {
        val instrId = map(security)
        val ret = QueueSimplifiedReader()
        val receiver = dist.add<AllTrades> { it is AllTrades }
        concurrentQueue.add {
            val lst = mutableListOf<AllTrades>()
            receiver.queue.drainTo(lst)
            val ticks = lst.flatMap { it.ticks }.filter { it.seccode == instrId.code && it.board == instrId.board }
            if(ticks.isNotEmpty()){
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
        stub.command(TrqCommandHelper.subscribe(instrId.code, instrId.board))
        return ret
    }
}
