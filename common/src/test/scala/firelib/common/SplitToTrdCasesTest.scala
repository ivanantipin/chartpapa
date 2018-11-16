package firelib.common

import java.time.Instant

import firelib.common.misc.utils
import org.junit.{Assert, Test}


class SplitToTrdCasesTest {


    @Test
    def TestSplittingToCases() = {

        val trds = List(
            new Trade(1, 10, new Order(OrderType.Market, 1, 10, Side.Buy,"sec","id", Instant.now()), Instant.now()),
            new Trade(2, 11, new Order(OrderType.Market, 2, 11, Side.Buy,"sec","id", Instant.now()), Instant.now()),
            new Trade(3, 12, new Order(OrderType.Market, 3, 12, Side.Buy,"sec","id", Instant.now()), Instant.now()),
            new Trade(1, 13, new Order(OrderType.Market, 1, 13, Side.Sell,"sec","id", Instant.now()), Instant.now()),
            new Trade(2, 14, new Order(OrderType.Market, 2, 14, Side.Sell,"sec","id", Instant.now()), Instant.now()),
            new Trade(3, 15, new Order(OrderType.Market, 3, 15, Side.Sell,"sec","id", Instant.now()), Instant.now())
        )

        var tcs = utils.toTradingCases(trds)



        Assert.assertEquals("must be 4 but it is " + tcs.length, tcs.length, 4)

        Assert.assertEquals("Qty must be 1 but it is " + tcs(0)._1.qty, tcs(0)._1.qty, 1)
        Assert.assertEquals("Qty must be 2 but it is " + tcs(1)._1.qty, tcs(1)._1.qty, 2)
        Assert.assertEquals("Qty must be 2 but it is " + tcs(2)._1.qty, tcs(2)._1.qty, 2)
        Assert.assertEquals("Qty must be 1 but it is " + tcs(3)._1.qty, tcs(3)._1.qty, 1)


        var pnls = tcs.map(utils.pnlForCase(_))

        Assert.assertEquals("Pnl must be 1 but it is " + pnls(0), pnls(0), 1, 0.0001)
        Assert.assertEquals("Pnl must be 4 but it is " + pnls(1), pnls(1), 4, 0.0001)
        Assert.assertEquals("Pnl must be 8 but it is " + pnls(2), pnls(2), 8, 0.0001)
        Assert.assertEquals("Pnl must be 5 but it is " + pnls(3), pnls(3), 5, 0.0001)
    }


}




