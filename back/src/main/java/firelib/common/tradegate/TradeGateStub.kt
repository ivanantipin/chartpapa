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

    //fixme have to be constructor
    fun init(){
        for(i in 0..modelConfig.instruments.size){
            val ticker =  modelConfig.instruments[i].ticker

            val b0 = BookStub(timeService, LimitOBook())
            secToBookLimit[ticker] = b0
            marketDataDistributor.listenOhlc(i, {b0.updateBidAsk(it.C - 0.005,it.C + 0.005)} )


            val b1 = BookStub(timeService, StopOBook())
            secToBookStop[ticker] = b1
            marketDataDistributor.listenOhlc(i, {b1.updateBidAsk(it.C - 0.005,it.C + 0.005)})

            val b2 = MarketOrderStub(timeService)
            secToMarketOrderStub[ticker] = b2
            marketDataDistributor.listenOhlc(i,{b2.updateBidAsk(it.C - 0.005,it.C + 0.005)})
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