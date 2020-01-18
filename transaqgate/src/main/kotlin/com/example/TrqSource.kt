package com.example

import com.firelib.Str
import com.firelib.TransaqConnectorGrpc
import com.funstat.domain.InstrId
import firelib.common.core.Source
import firelib.common.interval.Interval
import firelib.domain.Ohlc
import org.apache.commons.text.StringEscapeUtils
import java.time.LocalDateTime
import java.util.concurrent.Executors

class TrqSource(val stub : TransaqConnectorGrpc.TransaqConnectorBlockingStub) : Source {

    val dist = MsgCallbacker(stub, Executors.newSingleThreadExecutor())

    fun sendCmd(str: String): TrqResponse {
        return TrqParser.parseTrqResponse(StringEscapeUtils.unescapeJava(stub.sendCommand(Str.newBuilder().setTxt(str).build()).txt))
    }

    override fun symbols(): List<InstrId> {
        val fut = dist.getNext<Securities> { it is Securities }
        val response = sendCmd(TrqCommandHelper.securitiesCmd())
        return fut.get().securities.map {
            InstrId(id = it.seccode!!, board = it.board!!)
        }
    }

    override fun load(instrId: InstrId): Sequence<Ohlc> {
        TrqCommandHelper.subscribeCmd()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDefaultInterval(): Interval {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

fun main() {

    val client = TransaqGrpcClientExample("localhost", 50051)

    val blockingStub = client.blockingStub

    fun sendCmd(str: String): String {
        return StringEscapeUtils.unescapeJava(blockingStub.sendCommand(Str.newBuilder().setTxt(str).build()).txt)
    }

    println("login response ${sendCmd(TrqCommandHelper.connectCmd("TCNN9974", "v9D9z4", "tr1-demo5.finam.ru", "3939"))}")

    val executor = Executors.newSingleThreadExecutor()

    val source = TrqSource(blockingStub)

    source.symbols().forEach({
        println(it)
    })

}