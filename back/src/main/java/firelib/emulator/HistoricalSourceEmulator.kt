package firelib.emulator

import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import java.time.Instant
import java.time.LocalDateTime
import kotlin.random.Random

class HistoricalSourceEmulator() : HistoricalSource {

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

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        return sequence {
            val first = interval.ceilTime(Instant.now()).minusSeconds(9)

            repeat(10){
                yield(makeRandomOhlc(first.plusSeconds(it.toLong())))
            }
        }
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {
        return load(instrId, interval)
    }

    override fun mapSecurity(security: String): InstrId {
        return InstrId(code = security, source = SourceName.DUMMY.name)
    }

}