package com.firelib.transaq

import com.firelib.Str
import com.firelib.TransaqConnectorGrpc
import com.funstat.domain.InstrId
import firelib.common.core.Source
import firelib.common.core.timeSequence
import firelib.common.interval.Interval
import firelib.common.misc.toInstantDefault
import firelib.domain.Ohlc
import org.apache.commons.text.StringEscapeUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TrqSource(val stub : TransaqConnectorGrpc.TransaqConnectorBlockingStub, val kindId : String) : Source {


    val dist = MsgCallbacker(stub, Executors.newSingleThreadExecutor())

    fun sendCmd(str: String): TrqResponse {
        return TrqParser.parseTrqResponse(StringEscapeUtils.unescapeJava(stub.sendCommand(Str.newBuilder().setTxt(str).build()).txt))
    }

    override fun symbols(): List<InstrId> {
        val fut = dist.getNext<Securities> { it is Securities }
        val response = sendCmd(TrqCommandHelper.securitiesCmd())
        return fut.get().securities.map {
            InstrId(code = it.seccode!!, board = it.board!!)
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
        val candlesFut = dist.getContinuos<Candles> { it is Candles }
        val response = sendCmd(TrqCommandHelper.getHistory(instrId.code, instrId.board, kindId, 2000000))
        if(response.success){
            return sequence {
                while (true){
                    var candles = candlesFut.queue.poll(1, TimeUnit.MINUTES)
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
            return emptySequence()
        }
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc> {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getName(): String {
        return "TRQ"
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min1
    }

    val concurrentQueue = ConcurrentLinkedQueue<(Instant)->Unit>()

    init {
        Thread{
            timeSequence(Instant.now(), getDefaultInterval(), 100).forEach {time->
                concurrentQueue.forEach({it(time)})
            }
        }.start()
    }

    override fun listen(instrId: InstrId, callback: (Ohlc) -> Unit) {
        val queue = ConcurrentLinkedQueue<Tick>()

        concurrentQueue.add { time->
            if(queue.isNotEmpty()){
                callback(
                        Ohlc(endTime = time,
                                open = queue.first().price,
                                high = queue.maxBy { it.price }!!.price,
                                low = queue.minBy { it.price }!!.price,
                                close = queue.last().price,
                                volume = queue.sumBy { it.quantity }.toLong()
                        )

                )
            }
            queue.clear()
        }

        dist.addPlainListener {
            if(it is AllTrades){
                queue.addAll(it.ticks.filter { tick->
                    tick.board == instrId.board && tick.seccode == instrId.code
                })
            }
        }
    }

}

fun main() {


    val stub = makeDefaultStub()



    println("login response ${stub.command(TrqCommandHelper.connectCmd("TCNN9974", "v9D9z4", "tr1-demo5.finam.ru", "3939"))}")

    val executor = Executors.newSingleThreadExecutor()

    val dist = MsgCallbacker(stub, Executors.newSingleThreadExecutor())

    val next = dist.getNext<CandleKinds> { it is CandleKinds }

    val kinds = CandleKinds(kinds = listOf(Kind("1", period = 60, name = "5 min")))

    val source = TrqSource(stub, "1")

    val instrId = InstrId(code = "SBER", board = "TQBR")

    stub.command(TrqCommandHelper.subscribeCmd(arrayOf(instrId)))

    source.listen(instrId) {
        println(it)
    }




//    source.load(instrId).forEach {
//        println(it)
//    }

}