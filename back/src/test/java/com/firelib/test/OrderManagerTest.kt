package com.firelib.test

import com.funstat.domain.InstrId
import firelib.common.Order
import firelib.common.Trade
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.ordermanager.*
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.toSequence
import firelib.common.timeservice.TimeServiceManaged
import firelib.common.tradegate.TradeGateStub
import firelib.domain.OrderType
import firelib.domain.Side
import org.junit.Assert
import org.junit.Test
import java.time.Instant

class OrderManagerTest {

    fun  update(tg : TradeGateStub, bid : Double, ask : Double) : Unit {
        tg.limitBooks.forEach {it.updateBidAsk(bid, ask, Instant.now())}
        tg.stopBooks.forEach {it.updateBidAsk(bid, ask, Instant.now())}
        tg.marketSubs.forEach {it.updateBidAsk(bid,ask, Instant.now())}
    }

    @Test
    fun  TestMarketOrder() {
        var (tg, trades, om) = createStub()

        var bid = 1.5
        var ask = 2.5
        var qty = 2


        update(tg,bid,ask)

        om.makePositionEqualsTo(2)

        Assert.assertEquals(1, trades.size)
        Assert.assertEquals(qty, trades[0].qty)
        Assert.assertEquals(ask, trades[0].price, 0.001)
        Assert.assertEquals(2, om.position())

        var sellQty = 2

        om.makePositionEqualsTo(0)

        Assert.assertEquals(2, trades.size)
        Assert.assertEquals(sellQty, trades[1].qty)
        Assert.assertEquals(bid, trades[1].price, 0.001)
        Assert.assertEquals(0, om.position())

    }

    @Test
    fun  TestLimitOrderPriceBetterThanMarket() {
        var (tg, trades, om) = createStub()

        var bid = 1.5
        var ask = 2.5

        update(tg,bid,ask)

        var qty = 2

        om.buyAtLimit(3.0,qty)

        Assert.assertEquals(1, trades.size)
        Assert.assertEquals(qty, trades[0].qty)
        Assert.assertEquals(3.0, trades[0].price, 0.001)

        var sellQty = 2
        om.sellAtLimit(1.0,sellQty)

        Assert.assertEquals(2, trades.size)
        Assert.assertEquals(sellQty, trades[1].qty)
        Assert.assertEquals(1.0, trades[1].price, 0.001)
    }

    @Test
    fun  TestLimitOrder() {
        var (tg, trades, om) = createStub()

        var qty = 2
        var sellQty = 3


        update(tg,1.0, 3.0)

        om.buyAtLimit(1.5,qty)
        om.sellAtLimit(2.5,sellQty)

        Assert.assertEquals(0, trades.size)

        update(tg,2.6, 3.6)

        Assert.assertEquals(1, trades.size)
        Assert.assertEquals(sellQty, trades[0].qty)
        Assert.assertEquals(2.5, trades[0].price, 0.001)

        update(tg,1.0, 1.4)

        Assert.assertEquals(2, trades.size)
        Assert.assertEquals(qty, trades[1].qty)
        Assert.assertEquals(1.5, trades[1].price, 0.001)

    }

    @Test
    fun  TestStopOrder()  {
        var (tg, trades, om) = createStub()



        var qty = 2

        om.buyAtStop(2.5,qty)
        om.sellAtStop(1.5,qty)

        update(tg,1.5, 2.5)

        Assert.assertEquals(0, trades.size)

        update(tg,2.4, 3.0)

        Assert.assertEquals(1, trades.size)
        Assert.assertEquals(3.0, trades[0].price, 0.001)
        Assert.assertEquals(Side.Buy, trades[0].side())

        update(tg,1.4, 1.5)

        Assert.assertEquals(2, trades.size)
        Assert.assertEquals(1.4, trades[1].price, 0.001)
        Assert.assertEquals(Side.Sell, trades[1].side())

    }


    fun  createStub(): Triple<TradeGateStub, List<Trade>, OrderManager> {


        val timeService = TimeServiceManaged()

        val config = ModelBacktestConfig()
        config.instruments = listOf(InstrumentConfig("sec",{MarketDataReaderSql(emptyList()).toSequence()
        }, InstrId.dummyInstrument("sec")))

        val tg = TradeGateStub(config, timeService)

        timeService.updateTime(Instant.now())

        val om =  OrderManagerImpl(tg, timeService, "sec", 20,InstrId.dummyInstrument("sec"))
        val trades =  ArrayList<Trade>()
        om.tradesTopic().subscribe {trades += it}
        return Triple(tg, trades, om)
    }

    @Test
    fun  TestCloseAll()  {
        var (tg, trades, om) = createStub()

        var qty = 2
        var sellQty = 1

        update(tg,1.0, 3.0)

        om.buyAtLimit(1.5,qty)
        om.submitOrders( listOf( Order(OrderType.Market, 2.5, sellQty, Side.Sell,om.security(),"id", Instant.now(),  InstrId.dummyInstrument("sec"))))

        update(tg,1.0, 3.0)

        Assert.assertEquals(1, trades.size)
        Assert.assertEquals(-sellQty, om.position())

        Assert.assertEquals(1, om.liveOrders().size)

        //FIXME add test with delay and pending
        //Assert.assertEquals(om.hasPendingState, true)

        om.flattenAll()

        //Assert.assertEquals(om.hasPendingState, false)

        Assert.assertEquals(0, om.position())

        Assert.assertEquals(0, om.liveOrders().size)

        Assert.assertEquals(2, trades.size)

        Assert.assertEquals(Side.Buy, trades[1].side())

        Assert.assertEquals(sellQty, trades[1].qty)

    }

}


