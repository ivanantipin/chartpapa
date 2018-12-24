package firelib.common.core

import firelib.common.OrderStatus
import firelib.common.agenda.AgendaImpl
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.IntervalServiceImpl
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.misc.toStandardString
import firelib.common.model.Model
import firelib.common.reader.MarketDataReader
import firelib.common.reader.ReadersFactory
import firelib.common.timeboundscalc.TimeBoundsCalculator
import firelib.domain.Ohlc
import java.time.Instant

class Backtest(
        val intervalService: IntervalServiceImpl,
        val agenda: AgendaImpl,
        val marketDataDistributor: MarketDataDistributorImpl,
        val modelConfig: ModelBacktestConfig,
        val timeBoundsCalculator: TimeBoundsCalculator,
        val bindedModels: List<Model>,
        val readersFactory: ReadersFactory

) {

    val readerFunctions: List<() -> Unit>
    private val readers: List<MarketDataReader<Ohlc>>
    var readEnd = false

    class ReaderFunction(val reader: MarketDataReader<Ohlc>,
                         val index: Int,
                         val backtest: Backtest) : () -> Unit {


        val agenda = backtest.agenda
        val marketDataDistributor = backtest.marketDataDistributor
        val timeSeriesContainer = marketDataDistributor.timeseries[index]

        override operator fun invoke() {
            agenda.execute(reader.current().time(), this, 0)
            timeSeriesContainer.addOhlc(reader.current())
            if (!reader.read()) {
                backtest.readEnd = true
            }

        }
    }

    init {
        val bounds = timeBoundsCalculator(modelConfig)
        val time: Instant = intervalService.rootInterval().roundTime(bounds.first)
        this.readers = modelConfig.instruments.map { readersFactory(it, time) }
        readerFunctions = readers.mapIndexed { idx, reader ->
            ReaderFunction(reader, backtest = this, index = idx)
        }

        readers.forEachIndexed { idx, reader ->
            if (reader.current() != null) {
                agenda.execute(reader.current().dtGmtEnd, readerFunctions[idx], 0)
            } else {
                readEnd = true
            }
        }

        println("start time ${time.toStandardString()}")

        agenda.execute(time, this::stepFunc, 0)
    }


    fun stepFunc() {
        val currentTime = agenda.currentTime()
        intervalService.onStep(currentTime)
        bindedModels.forEach {it.update()}
        marketDataDistributor.roll(currentTime)
        agenda.execute(currentTime + intervalService.rootInterval().duration, this::stepFunc, 1)
    }



    fun backtest(): List<ModelOutput> {
        return backtestUntil(Instant.MAX)
    }

    fun backtestUntil(endDt: Instant): List<ModelOutput> {
        val ret = bindModelsToOutputs()
        while (!readEnd && agenda.currentTime().isBefore(endDt)) {
            agenda.next()
        }
        bindedModels.forEach { it.onBacktestEnd() }
        return ret
    }


    fun bindModelsToOutputs(): List<ModelOutput> {
        val ret = bindedModels.map { model ->
            val modelOutput = ModelOutput(model.properties())
            model.orderManagers().forEach { om -> om.tradesTopic().subscribe { modelOutput.trades += it } }
            model.orderManagers().forEach { om-> om.orderStateTopic().filter { it.status == OrderStatus.New }.subscribe { modelOutput.orderStates += it } }
            modelOutput
        }
        return ret
    }
}
