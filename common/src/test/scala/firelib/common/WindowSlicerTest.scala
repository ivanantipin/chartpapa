package firelib.common

import java.time.{Duration, Instant}

import firelib.common.interval.Interval
import firelib.common.misc.{NonDurableChannel, WindowSlicer}
import firelib.domain.Ohlc
import org.junit.{Assert, Test}

import scala.collection.mutable.ArrayBuffer

class WindowSlicerTest {

    @Test
    def testWSlicer(): Unit ={


        val in = new NonDurableChannel[Ohlc]()


        val wlen: Int = 3
        val slicer = new WindowSlicer[Ohlc](Duration.ofMinutes(wlen))

        val lst = new ArrayBuffer[Ohlc]()

        in.lift(slicer).subscribe(lst += _)

        val t0 = Interval.Min1.roundTime(Instant.now())

        val publish = 7

        val times = new ArrayBuffer[Instant]()

        for(i <- 0 until 20){
            val tm: Instant = t0.plus(Duration.ofMinutes(i))
            times += tm
            in.publish(Ohlc(tm,1,2,3,4))
            if(i == publish || i == publish + 2){
                slicer.updateWriteBefore()
            }
        }
        Assert.assertEquals(times.drop(publish - wlen).take(wlen*2 + 2).toSeq, lst.map(_.dtGmtEnd).toSeq)

    }

}
