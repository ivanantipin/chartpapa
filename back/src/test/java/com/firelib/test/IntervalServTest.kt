package com.firelib.test


import firelib.core.domain.Interval
import firelib.core.interval.IntervalServiceImpl
import org.junit.Assert
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.util.*


class IntervalTreeTest {

    @Test
    fun testIntervalService() {

        val service = IntervalServiceImpl()

        val time: Instant = Interval.Min60.roundTime(Instant.now())

        val lst = TreeSet<Interval>()

        service.addListener(Interval.Min1, { lst += Interval.Min1 })
        service.addListener(Interval.Min15, { lst += Interval.Min15 })
        service.addListener(Interval.Min10, { lst += Interval.Min10 })
        service.addListener(Interval.Min60, { lst += Interval.Min60 })


        service.onStep(time)

        Assert.assertEquals(listOf(Interval.Min1, Interval.Min10, Interval.Min15, Interval.Min60), lst.toList())

        lst.clear()

        service.onStep(time.plus(Duration.ofMinutes(10)))

        Assert.assertEquals(listOf(Interval.Min1, Interval.Min10), lst.toList())

        lst.clear()

        service.onStep(time.plus(Duration.ofMinutes(15)))

        Assert.assertEquals(listOf(Interval.Min1, Interval.Min15), lst.toList())

        lst.clear()

        service.onStep(time.plus(Duration.ofMinutes(30)))

        Assert.assertEquals(listOf(Interval.Min1, Interval.Min10, Interval.Min15), lst.toList())

    }


}
