package firelib.tcs

import firelib.core.domain.InstrId
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import org.slf4j.LoggerFactory
import ru.tinkoff.invest.openapi.data.CandleInterval
import ru.tinkoff.invest.openapi.data.StreamingEvent
import ru.tinkoff.invest.openapi.data.StreamingRequest
import ru.tinkoff.invest.openapi.wrapper.Context
import ru.tinkoff.invest.openapi.wrapper.impl.ConnectionFactory
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.Flow
import java.util.logging.Logger


fun getContext(): Context {
    val connect = ConnectionFactory.connectSandbox("t.OdqOG3D8_OaPqBn-m1wAD4uFYGWBK-1Em4VR_4te1es4PYjMaO288rF8IQ854J0cRpd3DhDCLPHuKi1mDFzpfw", Logger.getGlobal())
    return connect.get().context()
}

class TcsHistoricalSource(val context: Context) : HistoricalSource, Flow.Subscriber<StreamingEvent> {

    val log = LoggerFactory.getLogger(javaClass)

    override fun symbols(): List<InstrId> {

        return context.marketStocks.thenApply {
            it.instruments.map { inst ->
                InstrId(
                    id = inst.figi,
                    source = "TCS",
                    name = inst.name,
                    code = inst.ticker,
                    minPriceIncr = inst.minPriceIncrement,
                    lot = inst.lot
                )
            }
        }.join()
    }

    init {
        context.subscribe(this)
    }

    override fun load(instrId: InstrId): Sequence<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(300))
    }

    override fun load(instrId: InstrId, dateTimeIn: LocalDateTime): Sequence<Ohlc> {

        var dateTime = if(dateTimeIn < LocalDateTime.now().minusDays(300)){
            LocalDateTime.now().minusDays(300)
        }else{
            dateTimeIn
        }


        var dt = OffsetDateTime.of(dateTime, ZoneOffset.of("+03:00"))
        log.info("loading inst ${instrId} from ${dt}")
        return sequence {

            while (dt < OffsetDateTime.now()) {
                val candles = context.getMarketCandles(instrId.id,
                        dt, dt.plusDays(1), CandleInterval.ONE_MIN).join()

                yieldAll(candles.candles.map {
                    Ohlc(
                            endTime = it.time.toInstant() + Interval.Min1.duration,
                            open = it.o.toDouble(),
                            high = it.h.toDouble(),
                            low = it.l.toDouble(),
                            close = it.c.toDouble(),
                            volume = it.v.toLong(),
                            interpolated = false
                    )
                })
                dt = dt.plusDays(1)
            }
        }
    }

    override fun getName(): SourceName {
        return SourceName.TCS
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min1
    }


    var listener : ((Ohlc)->Unit) ? = null

    fun listen(instrId: InstrId, callback: (Ohlc) -> Unit) {
        listener = callback
        context.sendStreamingRequest(StreamingRequest.subscribeCandle(instrId.id, CandleInterval.ONE_MIN)).get()
    }

    override fun onComplete() {
        log.info("subscription complete")
    }

    override fun onSubscribe(p0: Flow.Subscription) {
        println("subscribed")
        p0.request(100000000L)
    }

    override fun onNext(event: StreamingEvent) {
        if(event is StreamingEvent.Candle){
            listener?.invoke(convertCaca(event))
        }
    }

    private fun convertCaca(caca: StreamingEvent.Candle) : Ohlc{
        return Ohlc(endTime = caca.dateTime.toInstant() + Interval.Min1.duration,
                open = caca.openPrice.toDouble(),
                high = caca.highestPrice.toDouble(),
                low = caca.lowestPrice.toDouble(),
                close = caca.closingPrice.toDouble(),
                volume = caca.tradingValue.toLong(),
                interpolated = false
        )
    }

    override fun onError(p0: Throwable?) {
        println("errorr ${p0}")
    }
}

fun main(){
    val mapper = TcsTickerMapper()
    val instr = mapper.map("sber")

    //mapper.source.listen(instr!!, {println(it)})


    var cnt = 0
    mapper.source.load(instr!!).forEach {
        cnt++
        println(cnt)
    }
    println(cnt)

    Thread.sleep(1000000)
}