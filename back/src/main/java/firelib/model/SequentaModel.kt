package firelib.model

import firelib.common.Trade
import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.merge
import firelib.core.misc.atNy
import firelib.core.misc.atUtc
import firelib.core.store.GlobalConstants
import firelib.core.store.MdStorageImpl
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.Signal
import firelib.indicators.sequenta.SignalType
import firelib.indicators.sequenta.calcStop
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs


/*
research model for simple breakout after low volatility period
 */


class SequentaModel(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    data class OpenPosition(val signal: Signal, val trade : Trade, val stopLoss : Double)

    data class PendingSignal(val date : LocalDate, val signal: Signal, val stopLoss: Double)

    var seqq : Sequenta? = null

    init {
        val startTime = LocalTime.of(10, 0, 0)
        val endTime = LocalTime.of(16, 0,0)

        val openPositions = arrayOfNulls<OpenPosition?>(instruments().size)

        enableSeries(Interval.Min30, interpolated = false).forEachIndexed { idx, halfHourTs ->
            var oh = Ohlc()

            var descriptor: PendingSignal? = null

            orderManagers()[idx].tradesTopic().subscribe {
                if (descriptor != null) {
                    openPositions[idx] = OpenPosition(descriptor!!.signal, it, descriptor!!.stopLoss)
                    descriptor = null
                }
            }


            val sequenta = Sequenta()

            seqq = sequenta

            halfHourTs.preRollSubscribe {
                val nyCurrentTime = currentTime().atNy()

                val nyLocalTime = nyCurrentTime.toLocalTime()

                if (nyLocalTime == startTime) {
                    oh = it[0]
                }else if(nyLocalTime in startTime..endTime){
                    oh = oh.merge(it[0])
                }

                if (nyLocalTime == endTime) {
                    val signals = sequenta.onOhlc(oh)
                    val signal = signals
                        .filter {
                            it.type == SignalType.Signal
                                    && it.reference.completedSignal == 13
                                    && it.reference.recycleRef == null
                        }
                        .find { true }

                    if (signal != null) {
                        descriptor = PendingSignal(
                            currentTime().atUtc().toLocalDate(),
                            signal,
                            sequenta.data.calcStop(signal.reference.up, signal.reference.start, sequenta.data.size)
                        )
                    }

                    if (descriptor != null) {


                        val close = sequenta.data.last().close
                        val close4 = sequenta.data[sequenta.data.size - 4].close

                        var money = 5000*close/Math.abs(descriptor!!.stopLoss - close)

                        money = Math.min(100_000.0, money)

                        val setup = descriptor!!.signal.reference

                        val setupEnd = sequenta.data[setup.end].close

                        val cond = abs(setupEnd - setup.tdst) > abs(close - setupEnd)

                        if (setup.isUp) {
                            if (close < descriptor!!.stopLoss && close < close4 && cond) {
                                shortForMoneyIfFlat(idx, money.toLong())
                            }
                        } else if (setup.isDown) {
                            if (close > descriptor!!.stopLoss && close > close4 && cond) {
                                longForMoneyIfFlat(idx, money.toLong())
                            }
                        }

                    }

                    if (position(idx) > 0) {
                        //reached target condition - tdst
                        if (halfHourTs[0].close > openPositions[idx]!!.signal.reference.tdst) {
                            flattenAll(idx, "reached target")
                        }
                        // stop loss
                        if (halfHourTs[0].close < openPositions[idx]!!.stopLoss) {
                            flattenAll(idx, "stop loss")
                        }
                    }
                    if (position(idx) < 0) {
                        // reached target
                        if (halfHourTs[0].close < openPositions[idx]!!.signal.reference.tdst) {
                            flattenAll(idx, "reached target")
                        }
                        // stop loss
                        if (halfHourTs[0].close > openPositions[idx]!!.stopLoss) {
                            flattenAll(idx, "stop loss")
                        }
                    }

                    if(position(idx) != 0 && positionDuration(idx) > 24*30){
                        flattenAll(idx,"time expiry")
                    }
                }
            }
            /*
            val forHTs = enableSeries(Interval.Min240)
            forHTs.forEachIndexed { idx, ts ->
                val sequenta = Sequenta()
                ts.preRollSubscribe {
                    val signals = sequenta.onOhlc(ts[0])


                    val signal = signals.filter { it.type == SignalType.Signal }.find { true }

                    if (signal != null && signal.reference.isUp && position(idx) > 0) {
                        flattenAll(idx)
                    }

                    if (signal != null && signal.reference.isDown && position(idx) < 0) {
                        flattenAll(idx)
                    }
                }
            }*/
        }
    }

    override fun onBacktestEnd() {
        println("===")
        seqq!!.data.forEach({oh->
            println(oh.toStrMtDay())
        })
        println("===")
    }

    fun transform() {
        val stt = MdStorageImpl()
        val instr = GlobalConstants.mdFolder.resolve("/ddisk/globaldatabase/1MIN/STK").toFile().list().toList()

        val idx = instr.indexOf("ON_1.csv")
        println("idx ${idx} out of instt ${instr.size}")
        instr.subList(idx + 1, instr.size)
            .map { it.replace("_1.csv", "") }.forEach({
                stt.transform(InstrId(code = it, source = SourceName.IQFEED.name), Interval.Min1, Interval.Min30)
            })
    }
}

fun seqModel(): ModelConfig {

    return ModelConfig(SequentaModel::class).apply {
        param("hold_hours", 30)
    }
}

fun main() {
    val start = System.currentTimeMillis()
    seqModel().runStrat(ModelBacktestConfig().apply {
        instruments = GlobalConstants.mdFolder.resolve("/ddisk/globaldatabase/1MIN/STK").toFile().list().toList()
            .map { it.replace("_1.csv", "") }.filter { it != "ON" && it != "ALL" }.subList(0, 200)
        interval = Interval.Min30
        startDate(LocalDate.now().minusDays(1000))
        histSourceName = SourceName.IQFEED
    })
    println((System.currentTimeMillis() - start)/1000.0)
    //transform()

}