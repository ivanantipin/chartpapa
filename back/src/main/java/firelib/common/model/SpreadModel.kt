package firelib.common.model

import firelib.common.core.ModelFactory
import firelib.common.interval.Interval
import firelib.common.interval.IntervalService
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.ordermanager.flattenAll
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.timeseries.TimeSeries
import firelib.common.timeservice.TimeService
import firelib.common.tradegate.TradeGate
import firelib.domain.Ohlc
import kotlin.math.abs


class ModelContext(val timeService : TimeService,
                   val intervalService : IntervalService,
                   val mdDistributor : MarketDataDistributor,
                   val tradeGate : TradeGate,
                   val instruments : List<String>){
}


class SmaFactory : ModelFactory {
    override fun invoke(context: ModelContext, props: Map<String, String>): Model {
        return SpreadModel(context,props)
    }
}

class SpreadModel(val context: ModelContext, val props: Map<String, String>) : Model{

    val ts0 : TimeSeries<Ohlc> = context.mdDistributor.getOrCreateTs(0, Interval.Day, props["period"]!!.toInt())
    val ts1 : TimeSeries<Ohlc> = context.mdDistributor.getOrCreateTs(1, Interval.Day, props["period"]!!.toInt())
    val period : Int = 10

    override fun update() {

        val r0 = (ts0[0].close - ts0[-10].close) / ts0[-10].close
        val r1 = (ts1[0].close - ts1[-10].close) / ts1[-10].close

        val spread = r0 - r1
        if(spread > 0.3){
            omanagers[0].makePositionEqualsTo(-1)
            omanagers[1].makePositionEqualsTo(1)
        }

        if(abs(spread) < 0.1){
            this.omanagers.forEach {it.flattenAll()}
        }

    }

    private var omanagers: List<OrderManagerImpl> = context.instruments
            .map { OrderManagerImpl(context.tradeGate,context.timeService,it)}


    override fun name(): String {
        return "Sma"
    }

    init {
        omanagers[0].tradesTopic().subscribe { println("trade ${it}") }
        omanagers[1].tradesTopic().subscribe { println("trade ${it}") }
    }

    override fun orderManagers(): List<OrderManager> {
        return this.omanagers
    }

    override fun onBacktestEnd() {
        this.omanagers.forEach {it.flattenAll()}
    }

    override fun properties(): Map<String, String> {
        return props
    }


}

