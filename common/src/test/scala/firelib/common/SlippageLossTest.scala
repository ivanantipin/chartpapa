package firelib.common

import java.time.Instant

import firelib.indicators.{OrderInfo, SlippageLoss}
import org.junit.{Assert, Test}

class SlippageLossTest {

    @Test
    def testAgenda(): Unit ={

        val loss = new SlippageLoss(Array(0,100))

        val now: Instant = Instant.now()

        val info: OrderInfo = new OrderInfo() {
            dt = now
            side = Side.Sell
            qty = 50
            maxPrice = 2.0
            minPrice = 1.0
            vwap = 1.7
        }//loss 0.3 * 50
        loss.add(info)
        Assert.assertEquals(15.0, loss(0),0.0001)
        Assert.assertEquals(0.0, loss(1),0.0001)

        info.qty = 100
        loss.add(info)
        Assert.assertEquals(15.0, loss(0),0.0001)
        Assert.assertEquals(30.0, loss(1),0.0001)

        info.side = Side.Buy
        loss.add(info) //loss 0.7 * 100
        Assert.assertEquals(100, loss(1),0.0001)



    }
}
