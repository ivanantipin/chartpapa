package firelib.core

import firelib.core.config.ModelBacktestConfig
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.mddistributor.MarketDataDistributor
import firelib.core.misc.PositionCloser
import firelib.core.misc.Quantiles
import firelib.core.timeseries.TimeSeries
import firelib.core.timeseries.nonInterpolatedView
import java.time.Instant
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class IdxContext(val model: Model, val idx: Int) {
    val om = model.orderManagers()[idx]
    fun moneyToLots(money: Long): Int {
        val close = model.context.mdDistributor.price(idx).close

        if(close < 0.000000001){
            throw RuntimeException("not valid price ${close}")
        }

        val ret = (money / close / om.instrument().lot).toInt()
        return ret
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

fun Model.enableSeries(interval: Interval,historyLen: Int = 100, interpolated: Boolean = true): List<TimeSeries<Ohlc>> {
    val context = this.modelContext()
    val ret = instruments().mapIndexed { idx, _ ->
        context.mdDistributor.getOrCreateTs(idx, interval, historyLen)
    }
    if (!interpolated) {
        return ret.map { it.nonInterpolatedView() }
    }
    return ret
}


data class GoogTrend(val word : String, val dt : Instant, val start : Instant, val idx : Long)

data class GoogTrendMulti(val dt : Instant, val word2idx : Map<String,Long> )


fun Model.quantiles(window : Int) : List<Quantiles<Double>>{
    return instruments().map {
        Quantiles<Double>(window);
    }
}

fun Model.position(idx : Int) : Int{
    return orderManagers()[idx].position()
}

fun Model.flattenAll(idx : Int, reason : String = "NA"){
    orderManagers()[idx].flattenAll(reason)
}

fun Model.tradeSize() : Long{
    return this.context.config.modelParams["trade_size"]!!.toLong()
}


fun Model.longForMoneyIfFlat(idx: Int, money: Long) : Boolean{
    return idxContext(idx).run {
        if (position <= 0 && !hasPendingState) {
            om.makePositionEqualsTo(moneyToLots(money), price)
            true
        }else{
            false
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
    val interval = this.context.config.runConfig.interval
    context.mdDistributor.getOrCreateTs(0,interval, 1).preRollSubscribe {
        orderManagers().forEachIndexed { index, orderManager ->
            if(orderManager.position() != 0 && condition(index)){
                orderManager.flattenAll()
            }
        }
    }
}

fun Model.prerollSubscribe(interval: Interval, listener : (Instant, MarketDataDistributor) -> Unit){
    context.mdDistributor.addListener(interval, listener)
}

fun Model.runConfig() : ModelBacktestConfig{
    return context.config.runConfig
}

fun Model.instruments() : List<String>{
    return context.config.runConfig.instruments
}

fun Model.positionDuration(idx : Int, unit : TimeUnit = TimeUnit.HOURS): Long {
    return orderManagers()[idx].positionDuration(currentTime(), unit)
}

