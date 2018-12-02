import com.funstat.iqfeed.IntervalTransformer
import firelib.common.interval.Interval
import firelib.domain.Ohlc
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class IntervalTransformerTest {

    @Test
    fun testWeek(){
        val mon = LocalDateTime.parse("3-Dec-2018").toInstant(ZoneOffset.UTC) //mon
        val fri = LocalDateTime.parse("7-Dec-2018").toInstant(ZoneOffset.UTC) //mon
        val nextMon = LocalDateTime.parse("10-Dec-2018").toInstant(ZoneOffset.UTC) //mon


/*
        IntervalTransformer.transform(Interval.Week, listOf(
                Ohlc(mon, 1.0,2.0,0.0,1.0),
        Ohlc(fri, 1.0,2.0,0.0,1.0),
        Ohlc(nextMon, 1.0,2.0,0.0,1.0)
        )
                )
*/

    }
}