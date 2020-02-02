package firelib.emulator

import firelib.core.domain.InstrId
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import java.time.Instant
import java.time.LocalDateTime
import kotlin.random.Random

class HistoricalSourceEmulator(val interval: Interval) : HistoricalSource {

    companion object{
        val SOURCE = SourceName.DUMMY
    }

    override fun getName(): SourceName {
        return SourceName.DUMMY
    }

    override fun symbols(): List<InstrId> {
        return listOf(
            InstrId(
                id = "1",
                name = "sber",
                market = "2",
                code = "sber",
                source = SOURCE.name
            )
        )
    }

    override fun getDefaultInterval(): Interval {
        return interval
    }

    fun makeRandomOhlc(time : Instant) : Ohlc {
        val open = Random.nextDouble(5.0, 10.0)
        val high = open + Random.nextDouble(1.0, 3.0)
        val low = open - Random.nextDouble(1.0, 3.0)
        return Ohlc(
            endTime = time,
            open = open,
            high = high,
            low = low,
            close = (high + low) / 2.0,
            volume = Random.nextLong(5, 100),
            interpolated = false
        )

    }

    override fun load(instrId: InstrId): Sequence<Ohlc> {
        return sequence {
            val first = interval.ceilTime(Instant.now()).minusSeconds(9)

            repeat(10){
                yield(makeRandomOhlc(first.plusSeconds(it.toLong())))
            }
        }
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc> {
        return load(instrId)
    }

}