import com.funstat.iqfeed.IntervalTransformer
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
        val mon = LocalDateTime.parse("2018-12-03 12:00:00",formatter).toInstant(ZoneOffset.UTC) //mon
        val fri = LocalDateTime.parse("2018-12-07 12:00:00",formatter).toInstant(ZoneOffset.UTC) //mon
        val nextMon = LocalDateTime.parse("2018-12-10 12:00:00",formatter).toInstant(ZoneOffset.UTC) //mon

        println(Date(1177718400000L))



        val out = IntervalTransformer.transform(Interval.Week, listOf(
                Ohlc(mon, 1.0, 2.0, 0.0, 1.0),
                Ohlc(fri, 2.0, 3.0, 1.0, 2.0),
                Ohlc(nextMon, 3.0, 4.0, 2.0, 3.0))
        )


        println(out)

        //println(LocalDateTime.ofInstant(Instant.ofEpochMilli(0 + Interval.Day.durationMs * 3) ,ZoneOffset.UTC).dayOfWeek)

        //Assert.assertEquals(3,out.size)


    }
}