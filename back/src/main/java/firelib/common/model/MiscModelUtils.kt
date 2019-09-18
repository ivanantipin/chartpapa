package firelib.common.model

import firelib.common.interval.Interval
import firelib.common.misc.PositionCloserByTimeOut
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.timeseries.TimeSeries
import firelib.common.timeseries.makeNonInterpolatedView
import firelib.domain.Ohlc
import java.time.Duration


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
        return ret.map { it.makeNonInterpolatedView() }
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


fun Model.closePositionByTimeout(days : Int = 0, hours : Int = 0, minutes : Int = 0, seconds : Int = 0){
    val dur = Duration.ofDays(days.toLong()) +
            Duration.ofHours(hours.toLong()) +
            Duration.ofMinutes(minutes.toLong()) +
            Duration.ofSeconds(seconds.toLong())
    val oms = this.orderManagers()
    val context = this.modelContext()
    oms.forEachIndexed({ idx, om ->
        PositionCloserByTimeOut(om, dur, context.mdDistributor, Interval.Min10, idx)
    })
}