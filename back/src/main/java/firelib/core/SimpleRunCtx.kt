package firelib.core

import firelib.core.store.DbReaderFactory
import firelib.core.config.ModelBacktestConfig
import firelib.core.mddistributor.MarketDataDistributorImpl
import firelib.model.Model
import firelib.model.ModelContext
import firelib.core.store.reader.SimplifiedReader
import firelib.core.timeservice.TimeServiceManaged
import firelib.core.backtest.tradegate.TradeGateStub
import firelib.core.domain.InstrId
import firelib.core.domain.ModelOutput
import firelib.core.domain.OrderStatus
import org.slf4j.LoggerFactory
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

    var gateMapper: InstrumentMapper = object : InstrumentMapper{
        override fun invoke(p1: String): InstrId {
            return InstrId(code = p1)
        }
    }

    val startTime = modelConfig.interval.roundTime(modelConfig.startDateGmt)

    val marketDataDistributor by lazy {
        MarketDataDistributorImpl(modelConfig.instruments.size, startTime, modelConfig.interval)
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