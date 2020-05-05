package firelib.mt5

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.toInstantDefault
import firelib.core.store.MdStorageImpl
import firelib.core.store.SourceFactory
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.lang.RuntimeException
import java.time.Instant
import java.time.LocalDateTime


data class Request(
    val action: String = "None",
    val actionType: String = "None",
    val symbol: String = "None",
    val chartTF: String = "None",
    val fromDate: String = "None",
    val toDate: String = "None",
    val id: String = "None",
    val magic: String = "None",
    val volume: String = "None",
    val price: String = "None",
    val stoploss: String = "None",
    val takeprofit: String = "None",
    val expiration: String = "None",
    val deviation: String = "None",
    val comment: String = "None"
)

val module = ObjectMapper().registerModule(KotlinModule())


class MTSafe : HistoricalSource{

    var del : MTraderAPI

    fun reinit() {
        del = MTraderAPI()
    }

    init {
        del = MTraderAPI()
    }

    override fun symbols(): List<InstrId> {
        return del.symbols()
    }

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        return del.load(instrId, interval)
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {

        try {
            return del.load(instrId, dateTime, interval)
        }catch (e : Exception){
            del.close()
            reinit()
            return del.load(instrId, dateTime, interval)
        }
    }

    override fun getName(): SourceName {
        return del.getName()
    }

}


class MTraderAPI(val host: String = "localhost") : HistoricalSource, AutoCloseable {


    val SYS_PORT = 15555
    val DATA_PORT = 15556
    val LIVE_PORT = 15557
    val EVENTS_PORT = 15558


    val sys_timeout = 1
    val data_timeout = 60


    val context = ZContext()

    val sys_socket: ZMQ.Socket
    val data_socket: ZMQ.Socket


    init {
        sys_socket = context.createSocket(SocketType.REQ)
        sys_socket.receiveTimeOut = sys_timeout * 1000
        sys_socket.connect("tcp://localhost:${SYS_PORT}")


        data_socket = context.createSocket(SocketType.PULL)
        data_socket.receiveTimeOut = data_timeout * 1000
        data_socket.connect("tcp://localhost:${DATA_PORT}")

    }

    override fun close() {
        context.close()
        sys_socket.close()
        data_socket.close()

        println("closed context")
    }


    fun sendRequest(data: Request) {
        val req = module.writeValueAsString(data)
        sys_socket.send(req)
        val rep = sys_socket.recvStr()
        if (rep != "OK") {
            println("err " + rep)
        }
    }


    fun pullReply(): String {
        val recvStr = data_socket.recvStr()
        return recvStr
    }


    fun construct_and_send(request: Request, interval: Interval): Sequence<Ohlc> {
        sendRequest(request)


        var tree = module.readTree(pullReply())

        while(tree["symbol"].asText() != request.symbol){
            println("continue as symbol does not match ${tree["symbol"].asText()} <> ${request.symbol}")
            tree = module.readTree(pullReply())
        }


        return sequence<Ohlc> {
            yieldAll(tree["data"].map {
                Ohlc(
                    endTime = Instant.ofEpochSecond(it[0].asLong() + interval.duration.toSeconds()),
                    open = it[1].asDouble(),
                    high = it[2].asDouble(),
                    low = it[3].asDouble(),
                    close = it[4].asDouble(),
                    volume = it[5].asDouble().toLong(),
                    interpolated = false
                )
            })

        }

    }

    override fun symbols(): List<InstrId> {
        TODO("Not yet implemented")
    }

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        TODO("Not yet implemented")
    }

    fun interval2str(interval: Interval): String {
        if (interval.name.startsWith("Min")) {
            return "M${interval.duration.toMinutes()}"
        }
        if (interval == Interval.Day) {
            return "D1"
        }
        throw RuntimeException("not supported")
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {

        val sec = dateTime.toInstantDefault().epochSecond

        return construct_and_send(
            Request(
                action = "HISTORY",
                actionType = "DATA",
                symbol = "${instrId.code}",
                chartTF = "${interval2str(interval)}",
                fromDate = "${sec}"
            ),interval
        )


    }

    override fun getName(): SourceName {
        return SourceName.MT5
    }
}

fun main() {
    val fac = SourceFactory()
    val storageImpl = MdStorageImpl()
    storageImpl.updateMarketData(InstrId(code = "ALLFUTRTSI", source = SourceName.MT5.name), Interval.Min15)
    //updateMt5(fac, storageImpl)
}

private fun updateMt5(fac: SourceFactory, storageImpl: MdStorageImpl) {
    val symbols = fac[SourceName.IQFEED].symbols()


//    storageImpl.updateMarketData(InstrId(code = "STL", source = SourceName.MT5.name), Interval.Min30)

    var counter = 0

    symbols.map {
        it.copy(source = SourceName.MT5.name)
    }.forEach {
        println("updating ${counter++} out of ${symbols.size}")
        storageImpl.updateMarketData(it, Interval.Min30)
    }
}

