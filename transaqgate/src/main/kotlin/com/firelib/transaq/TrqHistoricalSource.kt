package com.firelib.transaq

import com.firelib.TransaqConnectorGrpc
import com.funstat.domain.InstrId
import firelib.common.core.HistoricalSource
import firelib.common.core.InstrumentMapper
import firelib.common.core.SourceName
import firelib.common.interval.Interval
import firelib.common.misc.toInstantDefault
import firelib.common.reader.SimplifiedReader
import firelib.domain.Ohlc
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class TrqHistoricalSource(val stub : TransaqConnectorGrpc.TransaqConnectorBlockingStub, val kindId : String) : HistoricalSource {

    val dist = MsgCallbacker(stub)

    override fun symbols(): List<InstrId> {
        return dist.add<Securities> {it is Securities}.use {
            val response = stub.command(TrqCommandHelper.securitiesCmd())
            if(!response.success){
                throw RuntimeException("command did not succed ${response.message}")
            }
            val sec = it.queue.poll(5, TimeUnit.SECONDS)

            if(sec == null){
                return emptyList()
            }
            return sec.securities.map {
                InstrId(code = it.seccode!!, board = it.board!!)
            }
        }
    }

    /*
    status
    0 -данных больше нет (дочерпали до дна)
    1 -заказанное количество выдано (если надо еще -делать еще запрос)
    2 -продолжение следует (будет еще порция)
    3 -требуемые  данные  недоступны  (есть  смысл  попробовать  запросить позже)
     */

    internal var pattern = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    override fun load(instrId: InstrId): Sequence<Ohlc> {
        return dist.add<Candles> { it is Candles }.use {
            val queue = it.queue
            val response = stub.command(TrqCommandHelper.getHistory(instrId.code, instrId.board, kindId, 2000000))
            if(response.success){
                sequence {
                    while (true){
                        var candles = queue.poll(1, TimeUnit.MINUTES)
                        yieldAll(candles.candles.map {
                            Ohlc(endTime =  LocalDateTime.parse(it.date!!, pattern).toInstantDefault(),
                                open = it.open!!,
                                high = it.high!!,
                                low = it.low!!,
                                close = it.close!!,
                                volume = it.volume!!.toLong(),
                                interpolated = false

                            )
                        })
                        println("status ${candles.status}")
                        if(candles.status == "0" || candles.status == "1" || candles.status == "3"){
                            break
                        }
                    }
                }
            }else{
                emptySequence()
            }
        }
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc> {
        return load(instrId)
    }

    override fun getName(): SourceName {
        return SourceName.TRANSAQ
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min1
    }
}


class QueueSimplifiedReader : SimplifiedReader{
    val queue = LinkedBlockingQueue<Ohlc>()

    override fun peek(): Ohlc? {
        return queue.peek()
    }

    override fun poll(): Ohlc {
        return queue.poll()
    }
}

class TrqInstrumentMapper(val stub: TransaqConnectorGrpc.TransaqConnectorBlockingStub) : InstrumentMapper{

    val source = TrqHistoricalSource(stub, "1")
    val symbols = source.symbols().associateBy { it.code }

    override fun invoke(security: String): InstrId {
        return symbols[security]!!
    }

}



