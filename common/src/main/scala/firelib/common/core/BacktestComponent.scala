package firelib.common.core

import java.time.Instant

import ch.qos.logback.classic.{Level, Logger}
import firelib.common.agenda.AgendaComponent
import firelib.common.interval.IntervalServiceComponent
import firelib.common.mddistributor.MarketDataDistributorComponent
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.reader.{MarketDataReader, ReadersFactoryComponent}
import firelib.common.timeboundscalc.TimeBoundsCalculatorComponent
import firelib.common.timeservice.TimeServiceManagedComponent
import firelib.common.{MarketDataType, OrderStatus}
import firelib.domain.{Ohlc, Tick, Timed}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer

trait BacktestComponent {

    this: TimeBoundsCalculatorComponent
      with ModelConfigContext
      with ReadersFactoryComponent
      with MarketDataDistributorComponent
      with BindModelComponent
      with TimeServiceManagedComponent
      with AgendaComponent
      with IntervalServiceComponent =>

    val backtest = new Backtest

    LoggerFactory.getLogger(classOf[OrderManagerImpl]).asInstanceOf[Logger].setLevel(Level.ERROR)

    class Backtest {


        def stepFunc() : Unit = {
            intervalService.onStep(timeServiceManaged.currentTime)
            val nextTime = timeServiceManaged.currentTime.plus(intervalService.rootInterval.duration)
            agenda.addEvent(nextTime, stepFunc, 1)
        }

        var readEnd = false

        val readerFunctions = new Array[()=>Unit](modelConfig.instruments.length)

        def backtest(): Seq[ModelOutput] = {

            prepare()

            val ret = bindModelsToOutputs

            while (!readEnd){
                agenda.next()
            }

            bindedModels.foreach(_.onBacktestEnd())
            ret
        }

        def bindModelsToOutputs() : ArrayBuffer[ModelOutput] = {
            val ret = bindedModels.map(m => {
                val mo = new ModelOutput(m.properties)
                m.orderManagers.foreach(om => om.tradesTopic.subscribe(mo.trades += _))
                if(modelConfig.backtestMode == BacktestMode.SimpleRun){
                    m.orderManagers.foreach(om => om.orderStateTopic.filter(os => os.status == OrderStatus.New).subscribe(mo.orderStates += _))
                }
                mo
            })
            ret
        }

        def backtestUntil(endDt : Instant): Seq[ModelOutput] = {

            prepare()

            val ret = bindModelsToOutputs

            while (!readEnd && timeServiceManaged.currentTime.isBefore(endDt)){
                agenda.next()
            }

            bindedModels.foreach(_.onBacktestEnd())

            ret
        }


        def stepUntil(dtEnd : Instant): Unit = {
            while (timeServiceManaged.dtGmt.isBefore(dtEnd)){
                agenda.next()
            }
        }



        def prepare() {
            val bounds = timeBoundsCalculator.apply(modelConfig)

            val time: Instant =  intervalService.rootInterval.roundTime(bounds._1)

            val readers: Seq[MarketDataReader[Timed]] = modelConfig.instruments.map(readersFactory(_, time))

            timeServiceManaged.dtGmt = Instant.EPOCH

            for (idx <- 0 until modelConfig.instruments.length) {

                if (modelConfig.instruments(idx).mdType == MarketDataType.Ohlc) {
                    ohlcLambda(readers, idx)
                } else {
                    tickLambda(readers, idx)
                }
            }



            println(s"start time ${time.toStandardString}")

            marketDataDistributor.preInitCurrentBars(time)

            agenda.addEvent(time, stepFunc, 0)
        }

        def tickLambda(readers: Seq[MarketDataReader[Timed]], idx: Int) {
            val reader: MarketDataReader[Tick] = readers(idx).asInstanceOf[MarketDataReader[Tick]]
            readerFunctions(idx) = () => {
                marketDataDistributor.onTick(idx, reader.current)

                while (reader.read() && reader.current.dtGmt == timeServiceManaged.currentTime) {
                    marketDataDistributor.onTick(idx, reader.current)
                }

                if (reader.current == null) {
                    readEnd = true
                } else {
                    agenda.addEvent(reader.current.time, readerFunctions(idx),0)
                }
            }
            if (reader.current != null) {
                agenda.addEvent(reader.current.time, readerFunctions(idx),0)
            } else {
                readEnd = true
            }
        }

        def ohlcLambda(readers: Seq[MarketDataReader[Timed]], idx: Int) {
            val reader: MarketDataReader[Ohlc] = readers(idx).asInstanceOf[MarketDataReader[Ohlc]]
            readerFunctions(idx) = () => {
                marketDataDistributor.onOhlc(idx, reader.current)
                if (!reader.read()) {
                    readEnd = true
                } else {
                    agenda.addEvent(reader.current.time, readerFunctions(idx),0)
                }
            }
            if (reader.current != null) {
                agenda.addEvent(reader.current.dtGmtEnd, readerFunctions(idx),0)
            } else {
                readEnd = true
            }
        }
    }

}
