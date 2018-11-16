package firelib.common

import java.time.{Duration, Instant}

import firelib.common.interval.{Interval, IntervalServiceImpl}
import org.junit.{Assert, Test}

import scala.collection.mutable

class IntervalTreeTest {

    @Test
    def testIntervalService(): Unit ={

        val service = new IntervalServiceImpl

        val time: Instant = Interval.Min60.roundTime(Instant.now())

        val lst = new mutable.TreeSet[Interval]()(Ordering.by[Interval,Long](i=>i.durationMs))

        service.addListener(Interval.Min1,i=>{lst += Interval.Min1})
        service.addListener(Interval.Min15,i=>{lst += Interval.Min15})
        service.addListener(Interval.Min10,i=>{lst += Interval.Min10})
        service.addListener(Interval.Min60,i=>{lst += Interval.Min60})


        service.onStep(time)

        Assert.assertEquals(List(Interval.Min1,Interval.Min10,Interval.Min15,Interval.Min60), lst.toList)

        lst.clear()

        service.onStep(time.plus(Duration.ofMinutes(10)))

        Assert.assertEquals(List(Interval.Min1,Interval.Min10), lst.toList)

        lst.clear()

        service.onStep(time.plus(Duration.ofMinutes(15)))

        Assert.assertEquals(List(Interval.Min1,Interval.Min15), lst.toList)

        lst.clear()

        service.onStep(time.plus(Duration.ofMinutes(30)))

        Assert.assertEquals(List(Interval.Min1,Interval.Min10,Interval.Min15), lst.toList)

    }


}
