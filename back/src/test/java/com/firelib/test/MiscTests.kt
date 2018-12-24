package com.firelib.test

import firelib.common.Order
import firelib.common.OrderType
import firelib.common.Side
import firelib.common.Trade
import org.junit.Assert
import org.junit.Test
import java.time.Instant

class MiscTests{

    @Test
    fun testPosAdj(): Unit {
        val order = Order(OrderType.Market, 1.0, 10, Side.Buy,"sec","id", Instant.now())
        val trade = Trade(5, 1.0, order, Instant.now())
        val pos = trade.adjustPositionByThisTrade(10)
        Assert.assertEquals(pos , 15)
    }

}