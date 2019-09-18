package com.firelib.test

import firelib.common.Order
import firelib.domain.OrderType
import firelib.domain.Side
import firelib.common.Trade
import firelib.common.misc.pnlForCase
import firelib.common.misc.toTradingCases
import org.junit.Assert
import org.junit.Test
import java.time.Instant


class SplitTrdTest{

    @Test
    fun TestSplittingToCases() {

        val trds = listOf(
                Trade(1, 10.0, Order(OrderType.Market, 1.0, 10, Side.Buy,"sec","id", Instant.now()), Instant.now(), Instant.now()),
        Trade(2, 11.0, Order(OrderType.Market, 2.0, 11, Side.Buy,"sec","id", Instant.now()), Instant.now(), Instant.now()),
        Trade(3, 12.0, Order(OrderType.Market, 3.0, 12, Side.Buy,"sec","id", Instant.now()), Instant.now(), Instant.now()),
        Trade(1, 13.0, Order(OrderType.Market, 1.0, 13, Side.Sell,"sec","id", Instant.now()), Instant.now(), Instant.now()),
        Trade(2, 14.0, Order(OrderType.Market, 2.0, 14, Side.Sell,"sec","id", Instant.now()), Instant.now(), Instant.now()),
        Trade(3, 15.0, Order(OrderType.Market, 3.0, 15, Side.Sell,"sec","id", Instant.now()), Instant.now(), Instant.now())
        )

        var tcs = toTradingCases(trds)

        Assert.assertEquals("must be 4 but it is " + tcs.size, tcs.size, 4)

        Assert.assertEquals("Qty must be 1 but it is " + tcs[0].first.qty, tcs[0].first.qty, 1)
        Assert.assertEquals("Qty must be 2 but it is " + tcs[1].first.qty, tcs[1].first.qty, 2)
        Assert.assertEquals("Qty must be 2 but it is " + tcs[2].first.qty, tcs[2].first.qty, 2)
        Assert.assertEquals("Qty must be 1 but it is " + tcs[3].first.qty, tcs[3].first.qty, 1)


        var pnls = tcs.map({pnlForCase(it)})

        Assert.assertEquals("Pnl must be 1 but it is " + pnls[0], pnls[0], 1.0, 0.0001)
        Assert.assertEquals("Pnl must be 4 but it is " + pnls[1], pnls[1], 4.0, 0.0001)
        Assert.assertEquals("Pnl must be 8 but it is " + pnls[2], pnls[2], 8.0, 0.0001)
        Assert.assertEquals("Pnl must be 5 but it is " + pnls[3], pnls[3], 5.0, 0.0001)
    }


}