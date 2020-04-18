package firelib.core

import firelib.core.backtest.tradegate.TradeGateRiskManager
import firelib.core.backtest.tradegate.TradeGateStub
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.domain.ModelOutput
import firelib.core.mddistributor.MarketDataDistributorImpl
import firelib.core.store.reader.SimplifiedReader
import firelib.core.store.reader.skipUntil
import firelib.core.timeservice.TimeServiceManaged
import java.time.Instant


class SimpleRunCtx(val runConfig: ModelBacktestConfig) {

    val timeService by lazy {
        TimeServiceManaged()
    }

    val backtestGate by lazy {
        TradeGateStub(runConfig.instruments,
            timeService,
            runConfig.makeBidAdjuster(runConfig.spreadAdjustKoeff),
            runConfig.makeAskAdjuster(runConfig.spreadAdjustKoeff))
    }

    val tradeGate by lazy {
        TradeGateSwitch(backtestGate)
    }

    val riskTradeGate  = TradeGateRiskManager(runConfig.maxRiskMoney, tradeGate, runConfig.instruments)

    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(runConfig.instruments.size, runConfig.roundedStartTime(), runConfig.interval)
    }

    val boundModels = mutableListOf<ModelOutput>()

    fun makeContext(mc: ModelConfig): ModelContext {
        return ModelContext(
            timeService,
            marketDataDistributor,
            riskTradeGate,
            runConfig.gateMapper,
            mc
        )
    }


    fun addModel(params: Map<String, String>, mc: ModelConfig): Model {
        val model = mc.factory(makeContext(mc), params)
        val modelOutput = ModelOutput(model, model.properties())
        boundModels += modelOutput
        model.orderManagers().forEach { om -> om.tradesTopic().subscribe { modelOutput.trades += it } }
        model.orderManagers().forEach { om ->
            om.orderStateTopic().subscribe { modelOutput.orderStates += it }
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

        for (i in 0 until runConfig.instruments.size) {
            val oh = marketDataDistributor.price(i)
            tradeGate.backtestGate.updateBidAsks(i, oh.endTime, oh.close)
            riskTradeGate.updateBidAsks(i, oh.endTime, oh.close)
        }
        marketDataDistributor.roll(time)
    }

    fun backtest(endOfHistory: Instant): Instant {
        val factory = runConfig.backtestReaderFactory
        val readers = runConfig.instruments.map { factory.makeReader(it) }
        var currentTime = runConfig.roundedStartTime()

        readers.forEach {it.skipUntil(currentTime)}

        while (currentTime < endOfHistory) {
            progress(currentTime, readers)
            currentTime += this.runConfig.interval.duration
        }
        boundModels.forEach({it.model.onBacktestEnd()})
        return currentTime
    }
}