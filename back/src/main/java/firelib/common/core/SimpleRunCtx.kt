package firelib.common.core

import firelib.common.OrderStatus
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.model.Model
import firelib.common.model.ModelContext
import firelib.common.timeboundscalc.BacktestPeriodCalc
import firelib.common.timeservice.TimeServiceManaged
import firelib.common.tradegate.TradeGateStub
import java.time.Instant


class SimpleRunCtx(val modelConfig : ModelBacktestConfig){

    val tradeGate by lazy {
        TradeGateStub(modelConfig, timeService)
    }

    val timeService by lazy {
        TimeServiceManaged()
    }

    val rootInterval = Interval.Min1

    val startTime by lazy {
        val bounds = BacktestPeriodCalc.calcBounds(modelConfig)
        rootInterval.roundTime(bounds.first)
    }

    val readers by lazy {
        modelConfig.instruments.map { it.fact(startTime) }
    }

    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(readers)
    }

    val boundModels = mutableListOf<Model>()

    val modelContext by lazy {
        ModelContext(timeService,marketDataDistributor,tradeGate,modelConfig.instruments.map { it.ticker }, modelConfig)
    }

    fun addModel(factory : ModelFactory, params : Map<String,String>): Model {
        val ret = factory(modelContext, params)
        boundModels += ret
        return ret
    }

    fun backtest(until : Instant): List<ModelOutput> {
        marketDataDistributor.initTimes(startTime)
        val ret = bindModelsToOutputs()
        var ct = startTime
        while (ct.isBefore(until)){
            timeService.updateTime(ct)
            if(!marketDataDistributor.readUntil(ct)){
                break
            }
            tradeGate.updateBidAsks(readers.map { Pair(it.current().dtGmtEnd,it.current().close)})
            boundModels.forEach {it.update()}
            marketDataDistributor.roll(ct)
            ct += rootInterval.duration
        }
        boundModels.forEach { it.onBacktestEnd() }
        return ret
    }


    fun bindModelsToOutputs(): List<ModelOutput> {
        return boundModels.map { model ->
            val modelOutput = ModelOutput(model.properties())
            model.orderManagers().forEach { om -> om.tradesTopic().subscribe { modelOutput.trades += it } }
            model.orderManagers().forEach { om-> om.orderStateTopic().filter { it.status == OrderStatus.New }.subscribe { modelOutput.orderStates += it } }
            modelOutput
        }
    }


}