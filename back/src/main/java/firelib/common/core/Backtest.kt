package firelib.common.core

import firelib.common.OrderStatus
import firelib.common.agenda.Agenda
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.IntervalServiceImpl
import firelib.common.mddistributor.MarketDataDistributorImpl
import firelib.common.misc.toStandardString
import firelib.common.model.Model
import firelib.common.reader.MarketDataReader
import firelib.common.reader.ReadersFactory
import firelib.common.timeboundscalc.TimeBoundsCalculator
import firelib.common.timeservice.TimeServiceManaged
import firelib.domain.Ohlc
import firelib.domain.Timed
import java.time.Instant

class Backtest(
        val intervalService: IntervalServiceImpl,
        val timeServiceManaged: TimeServiceManaged,
        val agenda: Agenda,
        val marketDataDistributor: MarketDataDistributorImpl,
        val modelConfig: ModelBacktestConfig,
        val timeBoundsCalculator: TimeBoundsCalculator,
        val bindedModels: List<Model>,
        val readersFactory: ReadersFactory

) {

    fun stepFunc() {
        intervalService.onStep(timeServiceManaged.currentTime())
        val nextTime = timeServiceManaged.currentTime().plus(intervalService.rootInterval().duration)
        agenda.addEvent(nextTime, this::stepFunc, 1)
    }

    var readEnd = false


    fun backtest(): List<ModelOutput> {
        prepare()
        val ret = bindModelsToOutputs()
        while (!readEnd) {
            agenda.next()
        }
        bindedModels.forEach({ it.onBacktestEnd() })
        return ret
    }

    fun backtestUntil(endDt: Instant): List<ModelOutput> {
        prepare()
        val ret = bindModelsToOutputs()
        while (!readEnd && timeServiceManaged.currentTime().isBefore(endDt)) {
            agenda.next()
        }
        bindedModels.forEach({ it.onBacktestEnd() })
        return ret
    }


    fun bindModelsToOutputs(): List<ModelOutput> {
        val ret = bindedModels.map { m ->
            val modelOutput = ModelOutput(m.properties())
            m.orderManagers().forEach({ it.tradesTopic().subscribe({ modelOutput.trades += it }) })
            if (modelConfig.backtestMode == BacktestMode.SimpleRun) {
                m.orderManagers().forEach({ it.orderStateTopic().filter({ it.status == OrderStatus.New }).subscribe({ modelOutput.orderStates += it }) })
            }
            modelOutput
        }
        return ret
    }


    fun stepUntil(dtEnd: Instant): Unit {
        while (timeServiceManaged.dtGmt.isBefore(dtEnd)) {
            agenda.next()
        }
    }

    var readerFunctions: Array<() -> Unit>? = null


    fun prepare() {
        val bounds = timeBoundsCalculator(modelConfig)

        val time: Instant = intervalService.rootInterval().roundTime(bounds.first)

        val readers: List<MarketDataReader<Timed>> = modelConfig.instruments.map({ readersFactory(it, time) })

        timeServiceManaged.dtGmt = Instant.EPOCH

        for (idx in 0..modelConfig.instruments.size) {
            ohlcLambda(readers, idx)
        }

        println("start time ${time.toStandardString()}")

        //marketDataDistributor.preInitCurrentBars(time)

        agenda.addEvent(time, this::stepFunc, 0)
    }


    fun ohlcLambda(readers: List<MarketDataReader<Timed>>, idx: Int) {
        val reader: MarketDataReader<Ohlc> = readers[idx] as MarketDataReader<Ohlc>
        readerFunctions!![idx] = {
            marketDataDistributor.onOhlc(idx, reader.current())
            if (!reader.read()) {
                readEnd = true
            } else {
                agenda.addEvent(reader.current().time(), readerFunctions!![idx], 0)
            }
        }
        if (reader.current() != null) {
            agenda.addEvent(reader.current().dtGmtEnd, readerFunctions!![idx], 0)
        } else {
            readEnd = true
        }
    }
}
