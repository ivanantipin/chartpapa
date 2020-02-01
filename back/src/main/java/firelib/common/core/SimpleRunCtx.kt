package firelib.common.core

import firelib.store.DbReaderFactory
import firelib.common.config.ModelBacktestConfig
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.model.Model
import firelib.common.model.ModelContext
import firelib.common.reader.SimplifiedReader
import firelib.common.timeservice.TimeServiceManaged
import firelib.common.tradegate.TradeGateStub
import firelib.domain.OrderStatus
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

    var gateMapper: InstrumentMapper? = null


    val startTime = modelConfig.interval.roundTime(modelConfig.startDateGmt)

    val marketDataDistributor by lazy {
        println("start time is ${startTime}")
        MarketDataDistributorImpl(modelConfig.instruments.size, startTime)
    }

    val boundModels = mutableListOf<ModelOutput>()

    val modelContext by lazy {
        ModelContext(timeService, marketDataDistributor, tradeGate, gateMapper!!, modelConfig)
    }

    val backtestReaderFactory by lazy {
        DbReaderFactory(modelConfig.backtestHistSource.getName(), modelConfig.interval, modelConfig.roundedStartTime())
    }

    fun addModel(params: Map<String, String>): Model {
        val model = modelConfig.factory(modelContext, params)
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
            while (pick != null && pick.endTime <= this.startTime) {
                marketDataDistributor.addOhlc(idx, reader.poll())
                pick = reader.peek()
            }
        }

        timeService.updateTime(time)
        for (i in 0 until modelConfig.instruments.size) {
            val oh = marketDataDistributor.price(i)
            tradeGate.backtestGate.updateBidAsks(i, oh.endTime, oh.close)
        }
        boundModels.forEach { it.model.update() }
        marketDataDistributor.roll(time)
    }

    fun backtest(endOfHistory: Instant): Instant {
        val readers = modelConfig.instruments.map { backtestReaderFactory.makeReader(it) }
        var currentTime = startTime
        while (currentTime < endOfHistory) {
            progress(currentTime, readers)
            currentTime += this.modelConfig.interval.duration
        }
        return currentTime
    }

    fun addModelWithDefaultParams(): Model {
        require(boundModels.size == 0, { "only single model allowed for default params" })
        return addModel(modelConfig.modelParams)
    }
}