import java.time.Instant

import firelib.common._
import firelib.common.config.{InstrumentConfig, ModelBacktestConfig}
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.misc.SubChannel
import firelib.common.ordermanager.{OrderManager, OrderManagerImpl}
import firelib.common.timeseries.TimeSeries
import firelib.common.timeservice.{TimeServiceComponent, TimeServiceManagedComponent}
import firelib.common.tradegate.{TradeGateComponent, TradeGateStub}
import firelib.domain.{Ohlc, Tick}
import org.junit.{Assert, Test}

import scala.collection.mutable.ArrayBuffer


class OrderManagerTest {

    def update(tg : TradeGateStub, bid : Double, ask : Double) : Unit = {
        tg.secToBookLimit.values.foreach(b=>b.updateBidAsk(bid,ask))
        tg.secToBookStop.values.foreach(b=>b.updateBidAsk(bid,ask))
        tg.secToMarketOrderStub.values.foreach(b=>b.updateBidAsk(bid,ask))
    }

    @Test
    def TestMarketOrder() {
        var (tg, trades, om) = createStub()

        var bid = 1.5
        var ask = 2.5
        var qty = 2


        update(tg,bid,ask)

        om.managePosTo(2)

        Assert.assertEquals(1, trades.length)
        Assert.assertEquals(qty, trades(0).qty)
        Assert.assertEquals(ask, trades(0).price, 0.001)
        Assert.assertEquals(2, om.position)

        var sellQty = 2

        om.managePosTo(0)

        Assert.assertEquals(2, trades.length)
        Assert.assertEquals(sellQty, trades(1).qty)
        Assert.assertEquals(bid, trades(1).price, 0.001)
        Assert.assertEquals(0, om.position)

    }

    @Test
    def TestLimitOrderPriceBetterThanMarket() = {
        var (tg, trades, om) = createStub()

        var bid = 1.5
        var ask = 2.5

        update(tg,bid,ask)

        var qty = 2

        om.buyAtLimit(3,qty)

        Assert.assertEquals(1, trades.length)
        Assert.assertEquals(qty, trades(0).qty)
        Assert.assertEquals(3, trades(0).price, 0.001)

        var sellQty = 2
        om.sellAtLimit(1,sellQty)

        Assert.assertEquals(2, trades.length)
        Assert.assertEquals(sellQty, trades(1).qty)
        Assert.assertEquals(1, trades(1).price, 0.001)
    }

    @Test
    def TestLimitOrder() = {
        var (tg, trades, om) = createStub()

        var qty = 2
        var sellQty = 3


        update(tg,1, 3)

        om.buyAtLimit(1.5,qty)
        om.sellAtLimit(2.5,sellQty)

        Assert.assertEquals(0, trades.length)

        update(tg,2.6, 3.6)

        Assert.assertEquals(1, trades.length)
        Assert.assertEquals(sellQty, trades(0).qty)
        Assert.assertEquals(2.5, trades(0).price, 0.001)

        update(tg,1, 1.4)

        Assert.assertEquals(2, trades.length)
        Assert.assertEquals(qty, trades(1).qty)
        Assert.assertEquals(1.5, trades(1).price, 0.001)

    }

    @Test
    def TestStopOrder() = {
        var (tg, trades, om) = createStub()



        var qty = 2

        om.buyAtStop(2.5,qty)
        om.sellAtStop(1.5,qty)

        update(tg,1.5, 2.5)

        Assert.assertEquals(0, trades.length)

        update(tg,2.4, 3)

        Assert.assertEquals(1, trades.length)
        Assert.assertEquals(3, trades(0).price, 0.001)
        Assert.assertEquals(Side.Buy, trades(0).side)

        update(tg,1.4, 1.5)

        Assert.assertEquals(2, trades.length)
        Assert.assertEquals(1.4, trades(1).price, 0.001)
        Assert.assertEquals(Side.Sell, trades(1).side)

    }

    val mdDistr = new MarketDataDistributor {
        override def listenTicks(idx: Int, lsn: (Tick) => Unit): Unit = {null}

        override def listenOhlc(idx: Int, lsn: (Ohlc) => Unit): Unit = {null}

        override def setTickTransformFunction(fun: (Tick) => Tick): Unit = {}

        override def activateOhlcTimeSeries(tickerId: Int, interval: Interval, len: Int): TimeSeries[Ohlc] = {null}

        override def tickTopic(idx: Int): SubChannel[Tick] = ???

        override def getTs(tickerId: Int, interval: Interval): TimeSeries[Ohlc] = ???
    }

    private def createStub(): (TradeGateStub, Seq[Trade], OrderManager) = {

        val tg = new TradeGateComponent with TimeServiceManagedComponent with TimeServiceComponent {
            private val config: ModelBacktestConfig = new ModelBacktestConfig

            config.instruments += new InstrumentConfig("sec",null,MarketDataType.Tick)

            tradeGate = new TradeGateStub(mdDistr,config,timeServiceManaged)

            timeService = timeServiceManaged
        }

        tg.timeServiceManaged.dtGmt = Instant.now()

        val om = new OrderManagerImpl(tg, "sec")
        val trades = new ArrayBuffer[Trade]()
        om.tradesTopic.subscribe(trades += _)
        return (tg.tradeGate.asInstanceOf[TradeGateStub], trades, om)
    }

    @Test
    def TestCloseAll() = {
        var (tg, trades, om) = createStub()

        var qty = 2
        var sellQty = 1

        update(tg,1, 3)

        om.buyAtLimit(1.5,qty)
        om.submitOrders(new Order(OrderType.Market, 2.5, sellQty, Side.Sell,om.security,"id",Instant.now()))

        update(tg,1, 3)

        Assert.assertEquals(1, trades.length)
        Assert.assertEquals(-sellQty, om.position)

        Assert.assertEquals(1, om.liveOrders.toList.length)

        //FIXME add test with delay and pending
        //Assert.assertEquals(om.hasPendingState, true)

        om.flattenAll()

        //Assert.assertEquals(om.hasPendingState, false)

        Assert.assertEquals(0, om.position)

        Assert.assertEquals(0, om.liveOrders.toList.length)

        Assert.assertEquals(2, trades.length)

        Assert.assertEquals(Side.Buy, trades(1).side)

        Assert.assertEquals(sellQty, trades(1).qty)

    }

}


