package firelib.common.core

import firelib.common.agenda.AgendaImpl
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.IntervalServiceImpl
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.model.Model
import firelib.common.model.ModelContext
import firelib.common.reader.ReaderFactoryImpl
import firelib.common.timeboundscalc.TimeBoundsCalculatorImpl
import firelib.common.tradegate.TradeGateStub


typealias ModelFactory = (context : ModelContext, props : Map<String,String>) -> Model


class SimpleRunCtx(val modelConfig : ModelBacktestConfig){

    val tradeGate by lazy {
        TradeGateStub(modelConfig, agenda)
    }


    val intervalService by lazy {
        IntervalServiceImpl()
    }

    val agenda by lazy {
        AgendaImpl()
    }

    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(modelConfig)
    }

    val readersFactory by lazy {
        ReaderFactoryImpl(modelConfig)
    }

    val backtest by lazy {
        Backtest(intervalService,
                agenda,
                marketDataDistributor,
                modelConfig,
                timeBoundsCalculator, boundModels,readersFactory, tradeGate)
    }

    val timeBoundsCalculator by lazy {
        TimeBoundsCalculatorImpl(readersFactory)
    }

    val boundModels = ArrayList<Model>()

    val modelContext by lazy {
        ModelContext(agenda,intervalService,marketDataDistributor,tradeGate,modelConfig.instruments.map { it.ticker })
    }

    fun addModel(factory : ModelFactory, params : Map<String,String>){
        boundModels += factory(modelContext,params)
    }

}