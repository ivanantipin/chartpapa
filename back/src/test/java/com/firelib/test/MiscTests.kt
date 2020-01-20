package com.firelib.test

import com.funstat.domain.InstrId
import firelib.common.Order
import firelib.common.Trade
import firelib.common.interval.Interval
import firelib.domain.OrderType
import firelib.domain.Side
import org.junit.Assert
import org.junit.Test
import java.time.Instant

class MiscTests{

    @Test
    fun testPosAdj(): Unit {
        val order = Order(OrderType.Market, 1.0, 10, Side.Buy,"sec","id", Instant.now(), InstrId.dummyInstrument("sec"))
        val trade = Trade(5, 1.0, order, Instant.now(), Instant.now())
        val pos = trade.adjustPositionByThisTrade(10)
        Assert.assertEquals(pos , 15)
    }

    @Test
    fun testRoundInterval(){
        val next = Interval.Min1.ceilTime(Instant.ofEpochSecond(10))
        val next1 = Interval.Min1.ceilTime(Instant.ofEpochSecond(0))
        Assert.assertEquals(60000L, next.toEpochMilli() )
        Assert.assertEquals(0L, next1.toEpochMilli() )
    }

}