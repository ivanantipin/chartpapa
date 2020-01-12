package firelib.common.core

import firelib.common.OrderStatus
import firelib.common.config.ModelBacktestConfig
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.model.Model
import firelib.common.model.ModelContext
import firelib.common.timeservice.TimeServiceManaged
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

        modelConfig.rootInterval.roundTime(modelConfig.startDateGmt)
    }

    val marketDataDistributor by lazy {
        println("start time is ${startTime}")
        MarketDataDistributorImpl(modelConfig.instruments.size, startTime)
    }

    val boundModels = mutableListOf<ModelOutput>()

    fun addModel(params: Map<String, String>): Model {
        val factory = modelConfig.factory
        val ret = factory(ModelContext(timeService, marketDataDistributor, tradeGate, modelConfig), params)
        val modelOutput = ModelOutput(ret, ret.properties())
        boundModels += modelOutput
        ret.orderManagers().forEach { om -> om.tradesTopic().subscribe { modelOutput.trades += it } }
        ret.orderManagers().forEach { om -> om.orderStateTopic().filter { it.status == OrderStatus.New }.subscribe { modelOutput.orderStates += it } }
        return ret
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
}

fun SimpleRunCtx.addModelWithDefaultParams() : Model{
    require(boundModels.size == 0, {"only single model allowed for default params"})
    return addModel(modelConfig.modelParams)
}




fun SimpleRunCtx.backtest(endOfHistory: Instant, waitTillTime : (time : Instant)->Unit = {}): Instant {

    val readers = modelConfig.instruments.map { it.factory(startTime) }

    var stTime = this.startTime

    while (stTime < endOfHistory) {
        readers.forEachIndexed({ idx, reader ->
            var pick = reader.peek()
            while(pick != null && pick.endTime <= stTime){
                marketDataDistributor.addOhlc(idx, reader.poll())
                pick = reader.peek()
            }
        })

        time(stTime)
        stTime += this.modelConfig.rootInterval.duration
        waitTillTime(stTime)
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

    println("time to poll ${time}")

    val lst = mutableListOf<Ohlc>()

    while (this@pollOhlcsTill.peek() != null && this@pollOhlcsTill.peek().time() <= time) {
        lst.add(this@pollOhlcsTill.poll()!!)
    }

    return lst.groupBy { it.endTime }.values.map { it.last() }.asSequence()

}