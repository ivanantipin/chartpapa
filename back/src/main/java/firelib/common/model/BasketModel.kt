package firelib.common.model

import firelib.common.interval.Interval
import firelib.common.interval.IntervalService
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.ordermanager.flattenAll
import firelib.common.timeseries.TimeSeries
import firelib.common.timeservice.TimeService
import firelib.common.tradegate.TradeGate
import firelib.domain.Ohlc
import java.time.Instant

/**
 * main base class for all strategies
 */
class BasketModel(val timeService: TimeService,
                  val intervalService: IntervalService,
                  val marketDataDistributor: MarketDataDistributor,
                  val modelProps: Map<String, String>,
                  val tradeGate: TradeGate,
                  val instruments: Array<String>


) : Model {


    protected fun currentTime(): Instant {
        return timeService.currentTime()
    }

    val orderManagersFld: List<OrderManager>;

    init {
        orderManagersFld = instruments.map { OrderManagerImpl(tradeGate, timeService, it) }
    }


    //var bindComp : BindModelComponent , TimeServiceComponent , MarketDataDistributorComponent , IntervalServiceComponent = null

    override fun properties(): Map<String, String> = modelProps

    override fun name(): String = javaClass.getName()

    override fun orderManagers(): List<OrderManager> {
        return orderManagersFld;
    }


    /**
     * @param intr - interval to generate ohlc
     * @param lengthToMaintain length of enabled histories
     * @return return sequence of TimeSeries objects
     */
    fun enableOhlc(intr: Interval, lengthToMaintain: Int = -1): Array<TimeSeries<Ohlc>> {
        return Array(orderManagers().size) { marketDataDistributor.activateOhlcTimeSeries(it, intr, lengthToMaintain) }
    }


    protected fun listenInterval(interval: Interval, callback: (Interval) -> Unit) {
        intervalService.addListener(interval, { callback(interval) }, true)
    }

    fun ohlc(idx: Int, interval: Interval): TimeSeries<Ohlc> {
        return marketDataDistributor.getTs(idx, interval)
    }


    override fun onBacktestEnd() {
        orderManagers().forEach({ it.flattenAll() })
    }

}