import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.toInstantDefault
import firelib.iqfeed.IntervalTransformer
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

class IntervalTransformerTest {

    fun makeRandomOhlc(time: Instant): Ohlc {
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

    @Test
    fun testWeek() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val mon = LocalDateTime.parse("2018-12-03 00:00:01", formatter).toInstant(ZoneOffset.UTC) //mon
        val fri = LocalDateTime.parse("2018-12-07 12:00:00", formatter).toInstant(ZoneOffset.UTC) //mon
        val sun = LocalDateTime.parse("2018-12-09 23:59:59", formatter).toInstant(ZoneOffset.UTC) //mon
        val nextMon = LocalDateTime.parse("2018-12-10 00:00:01", formatter).toInstant(ZoneOffset.UTC) //mon
        val nextSun = sun.plusSeconds(3600 * 24 * 7)

        println(Date(1177718400000L))

        val out = IntervalTransformer.transform(
            Interval.Week, listOf(
                Ohlc(mon, 1.0, 2.0, 0.0, 1.0),
                Ohlc(fri, 2.0, 3.0, 1.0, 2.0),
                Ohlc(sun, 2.0, 3.5, 0.5, 2.0),
                Ohlc(nextMon, 3.0, 4.0, 2.0, 3.0),
                Ohlc(nextSun, 3.5, 4.5, 1.5, 3.5)
            )
        )
        println(out)
    }

    @Test
    fun testTransformer() {
        //LocalDateTime.parse("2022-09-01T09:33:54")
        val time = LocalDateTime.parse("2018-04-01T01:31:00").toInstantDefault()
        val time4 = LocalDateTime.parse("2018-04-01T01:40:00").toInstantDefault()
        val time2 = LocalDateTime.parse("2018-04-01T01:20:00").toInstantDefault()
        val ohlcs = listOf<Ohlc>(
            makeRandomOhlc(LocalDateTime.parse("2018-04-01T01:05:00").toInstantDefault()),
            makeRandomOhlc(LocalDateTime.parse("2018-04-01T01:06:00").toInstantDefault()),
            makeRandomOhlc(LocalDateTime.parse("2018-04-01T01:11:00").toInstantDefault()),
            makeRandomOhlc(time2),
            makeRandomOhlc(time)
        )

        val out = IntervalTransformer.transform(Interval.Min10, ohlcs)

        Assert.assertEquals(3, out.size)
        Assert.assertEquals(ohlcs.subList(0, 2).map { it.high }.maxOrNull(), out[0].high)
        Assert.assertEquals(ohlcs.subList(0, 2).map { it.low }.minOrNull(), out[0].low)
        Assert.assertEquals(out[0].close, ohlcs[1].close, 0.0000001)
        Assert.assertTrue(out[0].volume == ohlcs.subList(0, 2).sumBy { it.volume.toInt() }.toLong())
        Assert.assertTrue(out[1].high == ohlcs.subList(2, 4).map { it.high }.maxOrNull())
        Assert.assertTrue(out[1].low == ohlcs.subList(2, 4).map { it.low }.minOrNull())
        Assert.assertTrue(out[1].close == ohlcs[3].close)
        Assert.assertTrue(out[1].volume == ohlcs.subList(2, 4).sumBy { it.volume.toInt() }.toLong())
        Assert.assertEquals(out[2], ohlcs[4].copy(endTime = time4))
    }
}

