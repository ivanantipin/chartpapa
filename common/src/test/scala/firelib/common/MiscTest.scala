package firelib.common

import java.time.Instant

import firelib.domain.Ohlc
import org.junit.{Assert, Test}

/**

 */
class MiscTest {

    @Test
    def testPosAdj(): Unit ={
        val order: Order = new Order(OrderType.Market, 1.0, 10,Side.Buy,"sec","id", Instant.now())
        val trade: Trade = new Trade(5, 1.0, order, Instant.now())
        val pos = trade.adjustPositionByThisTrade(10)
        Assert.assertEquals(pos , 15)
    }

    @Test
    def testOhlcToString(): Unit ={
        new Ohlc().toString
    }


}
