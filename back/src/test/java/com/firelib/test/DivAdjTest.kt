package com.firelib.test

import firelib.common.model.Div
import firelib.common.reader.MarketDataReader
import firelib.common.reader.ReaderDivAdjusted
import firelib.domain.Ohlc
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
                Ohlc(startDt.atStartOfDay().toInstant(ZoneOffset.UTC),0.0,1.0,0.0,0.1),
                Ohlc(startDt.plusDays(1*10).st,0.1,1.1,0.1,0.2),
                Ohlc(start.plusSeconds(2*10),0.2,1.2,0.2,0.4),
                Ohlc(start.plusSeconds(3*10),0.3,1.3,0.3,0.5)
        )

        val divSize = 1.0
        Div("some", startDt.plusSeconds(15). )
        val divs = listOf( to divSize)

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
                return data[0].dtGmtEnd
            }

            override fun endTime(): Instant {
                return data.last().dtGmtEnd
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
