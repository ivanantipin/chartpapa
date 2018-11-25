package firelib.common.core

import firelib.common.agenda.AgendaImpl
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.IntervalServiceImpl
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.model.Model
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.reader.ReaderFactoryImpl
import firelib.common.timeboundscalc.TimeBoundsCalculatorImpl
import firelib.common.timeservice.TimeServiceManaged
import firelib.common.tradegate.TradeGateStub


typealias ModelFactory = (params: Map<String, String>)->Model


class SimpleRunCtx(val modelConfig : ModelBacktestConfig){

    val tradeGate by lazy {
        TradeGateStub(marketDataDistributor, modelConfig,timeService)
    }

    val timeService by lazy {
        TimeServiceManaged()
    }

    val intervalService by lazy {
        IntervalServiceImpl()
    }

    val agenda by lazy {
        AgendaImpl(timeService)
    }

    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(modelConfig, intervalService)
    }

    val readersFactory by lazy {
        ReaderFactoryImpl(modelConfig)
    }

    val backtest by lazy {
        Backtest(intervalService,
                timeService,
                agenda,
                marketDataDistributor,
                modelConfig,
                timeBoundsCalculator, boundModels,readersFactory)
    }

    val timeBoundsCalculator by lazy {
        TimeBoundsCalculatorImpl(readersFactory)
    }

    val boundModels = ArrayList<Model>()

    fun addModel(factory : ModelFactory, params : Map<String,String>){
        boundModels += factory(params)
    }

}