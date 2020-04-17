package firelib.core

import firelib.core.backtest.tradegate.TradeGateRiskManager
import firelib.core.backtest.tradegate.TradeGateStub
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.domain.ModelOutput
import firelib.core.domain.OrderStatus
import firelib.core.mddistributor.MarketDataDistributorImpl
import firelib.core.store.reader.SimplifiedReader
import firelib.core.store.reader.skipUntil
import firelib.core.timeservice.TimeServiceManaged
import java.time.Instant


class SimpleRunCtx(val modelConfig: ModelBacktestConfig) {

    val timeService by lazy {
        TimeServiceManaged()
    }

    val backtestGate by lazy {
        TradeGateStub(modelConfig, timeService)
    }

    val tradeGate by lazy {
        TradeGateSwitch(backtestGate)
    }

    val riskTradeGate  = TradeGateRiskManager(1000_000, tradeGate)


    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(modelConfig.instruments.size, modelConfig.roundedStartTime(), modelConfig.interval)
    }

    val boundModels = mutableListOf<ModelOutput>()

    fun makeContext(mc: ModelConfig): ModelContext {
        return ModelContext(
            timeService,
            marketDataDistributor,
            riskTradeGate,
            modelConfig.gateMapper,
            mc
        )
    }


    fun addModel(params: Map<String, String>, mc: ModelConfig): Model {
        val model = mc.factory(makeContext(mc), params)
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
            riskTradeGate.updateBidAsks(modelConfig.instruments[i], oh.close)
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
}