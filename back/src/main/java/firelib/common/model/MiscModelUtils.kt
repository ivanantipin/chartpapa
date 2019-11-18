package firelib.common.model

import firelib.common.interval.Interval
import firelib.common.misc.PositionCloser
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.timeseries.TimeSeries
import firelib.common.timeseries.nonInterpolatedView
import firelib.domain.Ohlc
import java.time.LocalTime


fun Model.enableFactor(name: String, fact: (Int) -> Double) {
    orderManagers().forEachIndexed { index, orderManager ->
        orderManager.tradesTopic().subscribe {
            it.tradeStat.addFactor(name, fact(index))
        }
    }
}

fun Model.enableSeries(interval : Interval,
                       historyLen : Int = 100, interpolated: Boolean = true) : List<TimeSeries<Ohlc>>{
    val context = this.modelContext()
    val ret = context.instruments.mapIndexed { idx, _ ->
        context.mdDistributor.getOrCreateTs(idx, interval, historyLen)

    }
    if(!interpolated){
        return ret.map { it.nonInterpolatedView() }
    }
    return ret

}

fun Model.buyIfNoPosition(idx : Int, money : Long){
    if(orderManagers()[idx].position() <= 0){
        val mm = money/this.context.mdDistributor.price(idx).close
        orderManagers()[idx].makePositionEqualsTo(mm.toInt())
    }
}

fun Model.sellIfNoPosition(idx : Int, money : Long){
    if(orderManagers()[idx].position() >= 0){
        val mm = money/this.context.mdDistributor.price(idx).close
        orderManagers()[idx].makePositionEqualsTo(-mm.toInt())
    }
}


fun Model.closePositionByTimeout(afterTime : LocalTime? = null,
                                 periods : Int,
                                 interval: Interval){
    PositionCloser.closePosByTimeoutAndTimeOfDay(this, afterTime,  periods, interval)
}