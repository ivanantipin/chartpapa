package firelib.common.tradegate

import firelib.common.Order
import firelib.common.OrderType
import firelib.common.config.ModelBacktestConfig
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.timeservice.TimeService


class TradeGateStub(val marketDataDistributor : MarketDataDistributor, val modelConfig : ModelBacktestConfig, val timeService : TimeService) : TradeGate{

    val secToBookLimit = HashMap<String,BookStub>()

    val secToBookStop = HashMap<String,BookStub>()

    val secToMarketOrderStub = HashMap<String,MarketOrderStub>()

    init{
        for(i in 0 until modelConfig.instruments.size){
            val ticker =  modelConfig.instruments[i].ticker

            val limitStub = BookStub(timeService, LimitOBook())
            secToBookLimit[ticker] = limitStub
            marketDataDistributor.listenOhlc(i) {limitStub.updateBidAsk(it.close - 0.005,it.close + 0.005)}


            val stopStub = BookStub(timeService, StopOBook())
            secToBookStop[ticker] = stopStub
            marketDataDistributor.listenOhlc(i) {stopStub.updateBidAsk(it.close - 0.005,it.close + 0.005)}

            val marketStub = MarketOrderStub(timeService)
            secToMarketOrderStub[ticker] = marketStub
            marketDataDistributor.listenOhlc(i) {marketStub.updateBidAsk(it.close - 0.005,it.close + 0.005)}
        }
    }

    /**
         * just order send
         */
    override fun sendOrder(order: Order){
         when(order.orderType) {
            OrderType.Limit -> secToBookLimit[order.security]!!.sendOrder(order)
            OrderType.Stop -> secToBookStop[order.security]!!.sendOrder(order)
            OrderType.Market -> secToMarketOrderStub[order.security]!!.sendOrder(order)
        }
    }

    /**
     * just order cancel
     */
    override fun cancelOrder(order: Order): Unit {
         when(order.orderType) {
            OrderType.Limit -> secToBookLimit[order.security]!!.cancelOrder(order)
            OrderType.Stop -> secToBookStop[order.security]!!.cancelOrder(order)
            //OrderType.Market -> ???
        }

    }
}