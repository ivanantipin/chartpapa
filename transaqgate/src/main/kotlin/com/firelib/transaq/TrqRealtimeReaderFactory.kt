package com.firelib.transaq

import com.firelib.TransaqConnectorGrpc
import firelib.core.domain.InstrId
import firelib.core.InstrumentMapper
import firelib.core.ReaderFactory
import firelib.core.timeSequence
import firelib.core.domain.Interval
import firelib.common.reader.QueueSimplifiedReader
import firelib.common.reader.SimplifiedReader
import firelib.core.domain.Ohlc
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
