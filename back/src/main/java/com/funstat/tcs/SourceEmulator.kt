package com.funstat.tcs

import com.funstat.domain.InstrId
import firelib.common.core.Source
import firelib.common.interval.Interval
import firelib.domain.Ohlc
import java.time.Instant
import java.time.LocalDateTime
import kotlin.random.Random

class SourceEmulator : Source {

    companion object{
        val SOURCE = "Dummy"
    }

    override fun getName(): String {
        return SOURCE
    }

    override fun symbols(): List<InstrId> {
        return listOf(InstrId(id = "1", name = "sber", market = "2", code = "sber", source = SOURCE))
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Sec1
    }


    override fun load(instrId: InstrId?): Sequence<Ohlc> {
        return sequence({
            val first = Interval.Sec1.ceilTime(Instant.now()).minusSeconds(9)

            repeat(10){

                val open = Random.nextDouble(5.0, 10.0)
                val high = open + Random.nextDouble(1.0, 3.0)
                val low = open - Random.nextDouble(1.0, 3.0)
                yield(Ohlc(
                        endTime = first.plusSeconds(it.toLong()),
                        open = open,
                        high = high,
                        low = low,
                        close = (high + low) / 2.0,
                        volume = Random.nextLong(5, 100),
                        interpolated = false
                ))
            }
        })
    }

    override fun load(instrId: InstrId?, dateTime: LocalDateTime?): Sequence<Ohlc> {
        return load(instrId)
    }

}