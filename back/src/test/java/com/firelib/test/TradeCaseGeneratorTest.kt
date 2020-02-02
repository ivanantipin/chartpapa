package com.firelib.test

import firelib.core.domain.InstrId
import firelib.common.Order
import firelib.common.Trade
import firelib.core.misc.pnl
import firelib.core.misc.toTradingCases
import firelib.core.domain.OrderType
import firelib.core.domain.Side
import org.junit.Assert
import org.junit.Test
import java.time.Instant


class TradeCaseGeneratorTest {

    @Test
    fun TestSplittingToCases() {

        val trds = listOf(
                Trade(1, 10.0, Order(OrderType.Market, 1.0, 10, Side.Buy, "sec", "id", Instant.now(), InstrId.dummyInstrument("sec")), Instant.now(), Instant.now()),
                Trade(2, 11.0, Order(OrderType.Market, 2.0, 11, Side.Buy, "sec", "id", Instant.now( ), InstrId.dummyInstrument("sec")), Instant.now(), Instant.now()),
                Trade(3, 12.0, Order(OrderType.Market, 3.0, 12, Side.Buy, "sec", "id", Instant.now( ), InstrId.dummyInstrument("sec")), Instant.now(), Instant.now()),
                Trade(3, 13.0, Order(OrderType.Market, 1.0, 13, Side.Sell, "sec", "id", Instant.now(), InstrId.dummyInstrument("sec")), Instant.now(), Instant.now()),
                Trade(2, 14.0, Order(OrderType.Market, 2.0, 14, Side.Sell, "sec", "id", Instant.now(), InstrId.dummyInstrument("sec")), Instant.now(), Instant.now()),
                Trade(1, 15.0, Order(OrderType.Market, 3.0, 15, Side.Sell, "sec", "id", Instant.now(), InstrId.dummyInstrument("sec")), Instant.now(), Instant.now())
        )

        var cases = trds.toTradingCases()

        Assert.assertEquals("must be 4 but it is " + cases.size, cases.size, 4)

        Assert.assertEquals("Qty must be 1 but it is " + cases[0].first.qty, 1, cases[0].first.qty)
        Assert.assertEquals("Qty must be 2 but it is " + cases[1].first.qty, 2, cases[1].first.qty)
        Assert.assertEquals("Qty must be 2 but it is " + cases[2].first.qty, 2, cases[2].first.qty)
        Assert.assertEquals("Qty must be 1 but it is " + cases[3].first.qty, 1, cases[3].first.qty)


        var pnls = cases.map({ it.pnl() })

        val expectedPnls = arrayListOf(3.0, 4.0, 4.0, 3.0)

        expectedPnls.forEachIndexed({idx, pnl->
            Assert.assertEquals("Pnl must be ${pnl} but it is ${pnls[idx]}"  , pnl, pnls[idx], 0.0001)
        })
    }

}