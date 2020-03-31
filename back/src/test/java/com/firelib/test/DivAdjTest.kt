package com.firelib.test

import firelib.core.misc.toInstantDefault
import firelib.model.Div
import firelib.core.store.reader.MarketDataReader
import firelib.core.store.reader.ReaderDivAdjusted
import firelib.core.domain.Ohlc
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


class DivAdjTest{
    @Test
    fun testDiv(){

        val start = Instant.now()
        val startDt = LocalDateTime.ofInstant(start, ZoneOffset.UTC).toLocalDate()
        val data = arrayOf(
                Ohlc(startDt.toInstantDefault(),0.0,1.0,0.0,0.1),
                Ohlc(startDt.plusDays(1*10).toInstantDefault(),0.1,1.1,0.1,0.2),
                Ohlc(startDt.plusDays(2*10).toInstantDefault(),0.2,1.2,0.2,0.4),
                Ohlc(startDt.plusDays(3*10).toInstantDefault(),0.3,1.3,0.3,0.5)
        )

        val divSize = 1.0

        val divs = listOf(Div("some", startDt.plusDays(15), divSize, ""))


        var reader = object : MarketDataReader<Ohlc>{
            var idx = 0
            override fun seek(time: Instant): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun current(): Ohlc {
                return data[idx]
            }

            override fun read(): Boolean {
                idx++
                return idx < data.size
            }

            override fun startTime(): Instant {
                return data[0].endTime
            }

            override fun endTime(): Instant {
                return data.last().endTime
            }

            override fun close() {

            }
        }
        val divAdj = ReaderDivAdjusted(reader, divs)
        Assert.assertEquals(divAdj.current().open, data[0].open, 0.0001)
        divAdj.read()
        Assert.assertEquals(divAdj.current().open, data[1].open, 0.0001)
        divAdj.read()
        Assert.assertEquals(divSize + data[2].open, divAdj.current().open, 0.0001)
        divAdj.read()
        Assert.assertEquals(divSize + data[3].open, divAdj.current().open,  0.0001)
        Assert.assertFalse(divAdj.read())
    }
}
