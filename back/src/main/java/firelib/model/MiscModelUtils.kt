package firelib.model

import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.flattenAll
import firelib.core.makePositionEqualsTo
import firelib.core.misc.PositionCloser
import firelib.core.misc.Quantiles
import firelib.core.timeseries.TimeSeries
import firelib.core.timeseries.nonInterpolatedView
import java.time.Instant
import java.time.LocalTime

class IdxContext(val model: Model, val idx: Int) {
    val om = model.orderManagers()[idx]
    fun moneyToLots(money: Long): Int {
        return (money / model.context.mdDistributor.price(idx).close / om.instrument().lot).toInt()
    }

    val price = model.context.mdDistributor.price(idx).close

    val position = om.position()

    val hasPendingState = om.hasPendingState()

}

fun Model.idxContext(idx: Int): IdxContext {
    return IdxContext(this, idx)
}

fun Model.enableFactor(name: String, fact: (Int) -> Double) {
    orderManagers().forEachIndexed { index, orderManager ->
        orderManager.tradesTopic().subscribe {
            it.tradeStat.addFactor(name, fact(index))
        }
    }
}

fun Model.currentTime() : Instant {
    return context.timeService.currentTime()
}

fun Model.enableSeries(interval: Interval,
                       historyLen: Int = 100, interpolated: Boolean = true): List<TimeSeries<Ohlc>> {
    val context = this.modelContext()
    val ret = context.config.instruments.mapIndexed { idx, _ ->
        context.mdDistributor.getOrCreateTs(idx, interval, historyLen)
    }

    if (!interpolated) {
        return ret.map { it.nonInterpolatedView() }
    }
    return ret
}

fun Model.quantiles(window : Int) : List<Quantiles<Double>>{
    return context.config.instruments.map {
        Quantiles<Double>(window);
    }
}

fun Model.longForMoneyIfFlat(idx: Int, money: Long) {
    idxContext(idx).run {
        if (position <= 0 && !hasPendingState) {
            om.makePositionEqualsTo(moneyToLots(money), price)
        }
    }
}

fun Model.shortForMoneyIfFlat(idx: Int, money: Long) {
    idxContext(idx).run {
        if (position >= 0 && !hasPendingState)
            om.makePositionEqualsTo(-moneyToLots(money), price)
    }
}

fun Model.closePositionByTimeout(afterTime: LocalTime? = null,
                                 periods: Int,
                                 interval: Interval
) {
    PositionCloser.closePosByTimeoutAndTimeOfDay(this, afterTime, periods, interval)
}

fun Model.closePosByCondition(condition : (idx: Int)->Boolean
) {
    val interval = this.context.config.interval
    context.mdDistributor.getOrCreateTs(0,interval, 1).preRollSubscribe {
        orderManagers().forEachIndexed { index, orderManager ->
            if(condition(index)){
                orderManager.flattenAll()
            }
        }
    }
}