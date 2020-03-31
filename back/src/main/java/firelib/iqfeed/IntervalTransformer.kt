package firelib.iqfeed

import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.merge
import firelib.core.misc.toInstantDefault
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.util.*
import kotlin.random.Random

object IntervalTransformer {

    fun transform(interval: Interval, ohlcs: List<Ohlc>): List<Ohlc> {
        if (ohlcs.isEmpty()) {
            return ohlcs
        }
        var init = ohlcs[0].copy(volume = 0)
        var nextTime = interval.ceilTime(ohlcs[0].endTime)
        val ret = ArrayList<Ohlc>()
        for (ohlc in ohlcs) {
            if (ohlc.endTime.isAfter(nextTime)) {
                ret.add(init.copy(endTime = nextTime))
                nextTime = interval.ceilTime(ohlc.endTime)
                init = ohlc
            }else{
                init = init.merge(ohlc)
            }
        }
        ret.add(init.copy(endTime = nextTime))

        return ret
    }
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


fun main() {
    val time = LocalDateTime.of(2018, Month.APRIL, 1, 1, 31).toInstantDefault()
    val time4 = LocalDateTime.of(2018, Month.APRIL, 1, 1, 40).toInstantDefault()
    val time2 = LocalDateTime.of(2018, Month.APRIL, 1, 1, 20).toInstantDefault()
    val ohlcs = listOf<Ohlc>(
        makeRandomOhlc(LocalDateTime.of(2018,Month.APRIL, 1, 1, 5).toInstantDefault()),
        makeRandomOhlc(LocalDateTime.of(2018,Month.APRIL, 1, 1, 6).toInstantDefault()),
        makeRandomOhlc(LocalDateTime.of(2018,Month.APRIL, 1, 1, 11).toInstantDefault()),
        makeRandomOhlc(time2),
        makeRandomOhlc(time)
    )

    val out = IntervalTransformer.transform(Interval.Min10, ohlcs)

    require(out.size ==3 )
    require(out[0].high == ohlcs.subList(0,2).map { it.high }.max() )
    require(out[0].low == ohlcs.subList(0,2).map { it.low }.min() )
    require(out[0].close == ohlcs[1].close)
    require(out[0].volume == ohlcs.subList(0,2).sumBy { it.volume.toInt() }.toLong())

    require(out[1].high == ohlcs.subList(2,4).map { it.high }.max() )
    require(out[1].low == ohlcs.subList(2,4).map { it.low }.min() )
    require(out[1].close == ohlcs[3].close)
    require(out[1].volume == ohlcs.subList(2,4).sumBy { it.volume.toInt() }.toLong())

    require(out[2] == ohlcs[4].copy(endTime = time4), {"not equal ${out[2]} and ${ohlcs[4]}"})
}
