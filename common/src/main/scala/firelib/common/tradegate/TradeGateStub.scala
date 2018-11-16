package firelib.common.tradegate

import firelib.common.config.ModelBacktestConfig
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.misc.SubChannel
import firelib.common.timeservice.TimeService
import firelib.common.{Order, OrderType, Trade}
import firelib.domain.OrderState

import scala.collection.mutable


class TradeGateStub(val marketDataDistributor : MarketDataDistributor, val modelConfig : ModelBacktestConfig, val timeService : TimeService) extends TradeGate{

    val secToBookLimit = new mutable.HashMap[String,BookStub]()

    val secToBookStop = new mutable.HashMap[String,BookStub]()

    val secToMarketOrderStub = new mutable.HashMap[String,MarketOrderStub]()


    for(i <- 0 until modelConfig.instruments.length){
        val ticker =  modelConfig.instruments(i).ticker
        secToBookLimit(ticker) = new BookStub(timeService) with LimitOBook
        marketDataDistributor.listenTicks(i,new TickBidAskUpdater((b,a)=>secToBookLimit(ticker).updateBidAsk(b,a)))
        marketDataDistributor.listenOhlc(i,new OhlcBidAskUpdater((b,a)=>secToBookLimit(ticker).updateBidAsk(b,a)))


        secToBookStop(ticker) = new BookStub(timeService) with StopOBook
        marketDataDistributor.listenTicks(i,new TickBidAskUpdater((b,a)=>secToBookStop(ticker).updateBidAsk(b,a)))
        marketDataDistributor.listenOhlc(i,new OhlcBidAskUpdater((b,a)=>secToBookStop(ticker).updateBidAsk(b,a)))

        secToMarketOrderStub(ticker) = new MarketOrderStub(timeService)
        marketDataDistributor.listenTicks(i,new TickBidAskUpdater((b,a)=>secToMarketOrderStub(ticker).updateBidAsk(b,a)))
        marketDataDistributor.listenOhlc(i,new OhlcBidAskUpdater((b,a)=>secToMarketOrderStub(ticker).updateBidAsk(b,a)))

    }

    /**
         * just order send
         */
    override def sendOrder(order: Order): (SubChannel[Trade], SubChannel[OrderState]) = {
        order.orderType match {
            case OrderType.Limit => secToBookLimit(order.security).sendOrder(order)
            case OrderType.Stop => secToBookStop(order.security).sendOrder(order)
            case OrderType.Market => secToMarketOrderStub(order.security).sendOrder(order)
        }

    }

    /**
     * just order cancel
     */
    override def cancelOrder(order: Order): Unit = {
        order.orderType match {
            case OrderType.Limit => secToBookLimit(order.security).cancelOrder(order)
            case OrderType.Stop => secToBookStop(order.security).cancelOrder(order)
            case OrderType.Market => ???
        }

    }
}
