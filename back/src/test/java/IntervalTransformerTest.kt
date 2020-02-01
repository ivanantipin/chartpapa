import firelib.iqfeed.IntervalTransformer
import firelib.common.interval.Interval
import firelib.domain.Ohlc
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class IntervalTransformerTest {

    @Test
    fun testWeek() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val mon = LocalDateTime.parse("2018-12-03 00:00:01",formatter).toInstant(ZoneOffset.UTC) //mon
        val fri = LocalDateTime.parse("2018-12-07 12:00:00",formatter).toInstant(ZoneOffset.UTC) //mon
        val sun = LocalDateTime.parse("2018-12-09 23:59:59",formatter).toInstant(ZoneOffset.UTC) //mon
        val nextMon = LocalDateTime.parse("2018-12-10 00:00:01",formatter).toInstant(ZoneOffset.UTC) //mon
        val nextSun =  sun.plusSeconds(3600*24*7)

        println(Date(1177718400000L))


        val out = IntervalTransformer.transform(Interval.Week, listOf(
                Ohlc(mon, 1.0, 2.0, 0.0, 1.0),
                Ohlc(fri, 2.0, 3.0, 1.0, 2.0),
                Ohlc(sun, 2.0, 3.5, 0.5, 2.0),
                Ohlc(nextMon, 3.0, 4.0, 2.0, 3.0),
                Ohlc(nextSun, 3.5, 4.5, 1.5, 3.5)
        )
        )


        println(out)

        //println(LocalDateTime.ofInstant(Instant.ofEpochMilli(0 + Interval.Day.durationMs * 3) ,ZoneOffset.UTC).dayOfWeek)

        //Assert.assertEquals(3,out.size)


    }
}