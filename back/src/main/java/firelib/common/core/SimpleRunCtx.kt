package firelib.common.core

import firelib.common.OrderStatus
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.model.Model
import firelib.common.model.ModelContext
import firelib.common.reader.ReaderFactoryImpl
import firelib.common.timeboundscalc.TimeBoundsCalculatorImpl
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

    val readersFactory by lazy {
        ReaderFactoryImpl(modelConfig)
    }

    val rootInterval = Interval.Min1

    val startTime by lazy {
        val bounds = timeBoundsCalculator(modelConfig)
        rootInterval.roundTime(bounds.first)
    }

    val readers by lazy {
        modelConfig.instruments.map { readersFactory(it, startTime) }
    }

    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(readers)
    }

    val timeBoundsCalculator by lazy {
        TimeBoundsCalculatorImpl(readersFactory)
    }

    val boundModels = ArrayList<Model>()

    val modelContext by lazy {
        ModelContext(timeService,marketDataDistributor,tradeGate,modelConfig.instruments.map { it.ticker })
    }

    fun addModel(factory : ModelFactory, params : Map<String,String>){
        boundModels += factory(modelContext,params)
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
            tradeGate.updateBidAsks(readers.map { it.current().close })
            boundModels.forEach {it.update()}
            marketDataDistributor.roll(ct)
            ct += rootInterval.duration
        }
        boundModels.forEach { it.onBacktestEnd() }
        return ret
    }


    fun bindModelsToOutputs(): List<ModelOutput> {
        val ret = boundModels.map { model ->
            val modelOutput = ModelOutput(model.properties())
            model.orderManagers().forEach { om -> om.tradesTopic().subscribe { modelOutput.trades += it } }
            model.orderManagers().forEach { om-> om.orderStateTopic().filter { it.status == OrderStatus.New }.subscribe { modelOutput.orderStates += it } }
            modelOutput
        }
        return ret
    }


}