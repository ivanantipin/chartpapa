package firelib.core

import firelib.core.backtest.tradegate.TradeGateStub
import firelib.core.config.ModelBacktestConfig
import firelib.core.domain.ModelOutput
import firelib.core.domain.OrderStatus
import firelib.core.mddistributor.MarketDataDistributorImpl
import firelib.core.store.reader.SimplifiedReader
import firelib.core.store.reader.skipUntil
import firelib.core.timeservice.TimeServiceManaged
import firelib.model.Model
import firelib.model.ModelContext
import java.time.Instant


class SimpleRunCtx(val modelConfig: ModelBacktestConfig) {

    val backtestGate by lazy {
        TradeGateStub(modelConfig, timeService)
    }

    val tradeGate by lazy {
        TradeGateSwitch(backtestGate)
    }

    val timeService by lazy {
        TimeServiceManaged()
    }

    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(modelConfig.instruments.size, modelConfig.roundedStartTime(), modelConfig.interval)
    }

    val boundModels = mutableListOf<ModelOutput>()

    fun makeContext(): ModelContext {
        return ModelContext(timeService, marketDataDistributor, tradeGate, modelConfig.gateMapper!!, modelConfig)
    }


    fun addModel(params: Map<String, String>): Model {
        val model = modelConfig.factory(makeContext(), params)
        val modelOutput = ModelOutput(model, model.properties())
        boundModels += modelOutput
        model.orderManagers().forEach { om -> om.tradesTopic().subscribe { modelOutput.trades += it } }
        model.orderManagers().forEach { om ->
            om.orderStateTopic().filter { it.status == OrderStatus.New }.subscribe { modelOutput.orderStates += it }
        }
        return model
    }

    fun progress(time: Instant, readers: List<SimplifiedReader>) {
        readers.forEachIndexed { idx, reader ->
            var pick = reader.peek()
            while (pick != null && pick.endTime <= time) {
                marketDataDistributor.addOhlc(idx, reader.poll())
                pick = reader.peek()
            }
        }

        timeService.updateTime(time)
        for (i in 0 until modelConfig.instruments.size) {
            val oh = marketDataDistributor.price(i)
            tradeGate.backtestGate.updateBidAsks(i, oh.endTime, oh.close)
        }
        marketDataDistributor.roll(time)
    }

    fun backtest(endOfHistory: Instant): Instant {
        val factory = modelConfig.backtestReaderFactory
        val readers = modelConfig.instruments.map { factory.makeReader(it) }
        var currentTime = modelConfig.roundedStartTime()

        readers.forEach {it.skipUntil(currentTime)}

        while (currentTime < endOfHistory) {
            progress(currentTime, readers)
            currentTime += this.modelConfig.interval.duration
        }
        boundModels.forEach({it.model.onBacktestEnd()})
        return currentTime
    }

    fun addModelWithDefaultParams(): Model {
        require(boundModels.size == 0, { "only single model allowed for default params" })
        return addModel(modelConfig.modelParams)
    }
}