package firelib.common.core

import firelib.common.Order
import firelib.common.OrderStatus
import firelib.common.config.ModelBacktestConfig
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.model.ModelContext
import firelib.common.reader.pollOhlcsTill
import firelib.common.timeboundscalc.BacktestPeriodCalc
import firelib.common.timeservice.TimeServiceManaged
import firelib.common.tradegate.TradeGate
import firelib.common.tradegate.TradeGateStub
import firelib.domain.Ohlc
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue


class SimpleRunCtx(val modelConfig: ModelBacktestConfig) {

    val tradeGateStub by lazy {
        TradeGateStub(modelConfig, timeService)
    }

    val tradeGate by lazy {
        TradeGateSwitch(tradeGateStub)
    }

    val timeService by lazy {
        TimeServiceManaged()
    }

    val startTime by lazy {
        val bounds = BacktestPeriodCalc.calcBounds(modelConfig)
        modelConfig.rootInterval.roundTime(bounds.first)
    }

    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(modelConfig.instruments.size, startTime)
    }

    val batchers = mutableListOf<Batcher<*>>()

    fun cancelBatchersAndWait(){
        batchers.forEach {it.cancelAndJoin()}
    }

    val boundModels = mutableListOf<ModelOutput>()


    fun addModel(factory: ModelFactory, params: Map<String, String>) {
        val ret = factory(ModelContext(timeService, marketDataDistributor, tradeGate, modelConfig), params)
        val modelOutput = ModelOutput(ret, ret.properties())
        boundModels += modelOutput
        ret.orderManagers().forEach { om -> om.tradesTopic().subscribe { modelOutput.trades += it } }
        ret.orderManagers().forEach { om -> om.orderStateTopic().filter { it.status == OrderStatus.New }.subscribe { modelOutput.orderStates += it } }
    }

    fun time(ct: Instant) {
        timeService.updateTime(ct)
        for (i in 0 until modelConfig.instruments.size) {
            val oh = marketDataDistributor.price(i)
            tradeGate.backtestGate.updateBidAsks(i, oh.endTime, oh.close)
        }
        boundModels.forEach { it.model.update() }
        marketDataDistributor.roll(ct)
    }

    fun onStart() {
    }

    fun onEnd() {
        boundModels.forEach { it.model.onBacktestEnd() }
    }


}

class TradeGateSwitch(val backtestGate : TradeGateStub) : TradeGate{

    var delegate : TradeGate = backtestGate

    fun setActiveReal(realGate : TradeGate){
        delegate = realGate
    }

    override fun cancelOrder(order: Order) {
        delegate.cancelOrder(order)
    }

    override fun sendOrder(order: Order) {
        delegate.sendOrder(order)
    }

}


fun SimpleRunCtx.backtest(endOfHistory: Instant): Instant {

    val readers = modelConfig.instruments.map { it.factory(startTime) }

    var stTime = this.startTime

    while (stTime < endOfHistory) {
        readers.forEachIndexed({ idx, reader ->
            reader.pollOhlcsTill(stTime).forEach {
                marketDataDistributor.addOhlc(idx, it)
            }
        })
        time(stTime)
        stTime += this.modelConfig.rootInterval.duration
    }
    return stTime;

}


fun waitUntil(timestamp: Instant) {

    val millis = timestamp.toEpochMilli() - System.currentTimeMillis()

    if (millis <= 0) return
    try {
        Thread.sleep(millis)
    } catch (e: InterruptedException) {
        throw RuntimeException(e.message, e)
    }
}


fun LinkedBlockingQueue<Ohlc>.pollOhlcsTill(time: Instant): Sequence<Ohlc> {
    return sequence({
        while (this@pollOhlcsTill.peek() != null && this@pollOhlcsTill.peek().time() <= time) {
            yield(this@pollOhlcsTill.poll()!!)
        }
    })
}


