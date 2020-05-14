package firelib.mt5

import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.toInstantDefault
import firelib.core.store.GlobalConstants
import firelib.core.store.MdDaoContainer
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.lang.RuntimeException
import java.time.Instant
import java.time.LocalDateTime

class MT5Source(val host: String = "localhost") : HistoricalSource, AutoCloseable {


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


    fun sendRequest(data: Mt5Request) {
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


    fun construct_and_send(mt5Request: Mt5Request, interval: Interval): Sequence<Ohlc> {
        sendRequest(mt5Request)


        var tree = module.readTree(pullReply())

        while(tree["symbol"].asText() != mt5Request.symbol){
            println("continue as symbol does not match ${tree["symbol"].asText()} <> ${mt5Request.symbol}")
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
        return MdDaoContainer().getDao(SourceName.MT5, Interval.Min30).listAvailableInstruments().map {
            InstrId(code = it, source = SourceName.MT5.name)
        }
    }

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(6000), interval)
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
            Mt5Request(
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