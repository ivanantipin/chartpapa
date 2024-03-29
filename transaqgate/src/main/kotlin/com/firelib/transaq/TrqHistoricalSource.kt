package com.firelib.transaq

import com.firelib.TransaqConnectorGrpc
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.toInstantDefault
import firelib.core.store.trqMapperWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class TrqHistoricalSource(val stub : TransaqConnectorGrpc.TransaqConnectorBlockingStub, val kindId : String) : HistoricalSource {

    val dist = TrqMsgDispatcher(stub)

    override fun symbols(): List<InstrId> {
        return dist.add<Securities> {it is Securities}.use {
            val response = stub.command(TrqCommandHelper.securitiesCmd())
            if(!response.success){
                throw RuntimeException("command did not succeed ${response.message}")
            }
            val sec = it.queue.poll(60, TimeUnit.SECONDS)

            if(sec == null){
                return emptyList()
            }
            return sec.securities.map {
                InstrId(code = it.seccode!!, board = it.board!!, lot = it.lotsize!!.toInt(), minPriceIncr = it.minstep!!.toBigDecimal(), name = it.shortname!!, id = it.secid!!)
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

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        //fixme hook with interval
        return dist.add<Candles> { it is Candles }.use {
            val queue = it.queue
            val response = stub.command(TrqCommandHelper.getHistory(instrId.code, instrId.board, kindId, 2000_000))
            println(response)
            if(response.success){
                sequence {
                    while (true){

                        var candles = queue.poll(1, TimeUnit.MINUTES)
                        if(candles == null){
                            break
                        }
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

    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {
        return load(instrId, interval)
    }

    override fun getName(): SourceName {
        return SourceName.TRANSAQ
    }
}

fun main() {
    val stub = makeDefaultStub()
    val dispatcher = TrqMsgDispatcher(stub)

    stub.command(loginCmd)

    val source = TrqHistoricalSource(stub, "2")

    enableMsgLogging(dispatcher,{it is Candles})

    val instr = trqMapperWriter().read().filter { it.code == "SBER" }.first()

    val lst = source.load(instr, Interval.Min5).toList()
    println(lst.first())
    println(lst.last())


}