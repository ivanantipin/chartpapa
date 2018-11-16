package firelib.common.model

import java.time.Instant

import firelib.common.ModelInitResult
import firelib.common.core.BindModelComponent
import firelib.common.interval.{AllIntervals, Interval, IntervalServiceComponent}
import firelib.common.mddistributor.MarketDataDistributorComponent
import firelib.common.misc.{DateUtils, SubChannel}
import firelib.common.ordermanager.OrderManager
import firelib.common.timeseries.OhlcSeries
import firelib.common.timeservice.TimeServiceComponent
import firelib.domain.Tick

import scala.collection.immutable.IndexedSeq

/**
 * main base class for all strategies
 */
abstract class BasketModel extends Model with DateUtils with MiscModelUtils with AllIntervals{


    /**
     * variable holds model properties
     */
    private var modelProperties: Map[String, String] = _

    protected def currentTime: Instant  = bindComp.timeService.currentTime

    var orderManagersFld: Array[OrderManager] =_

    var bindComp : BindModelComponent with TimeServiceComponent with MarketDataDistributorComponent with IntervalServiceComponent = null

    override def properties: Map[String, String] = modelProperties

    override def name: String = getClass.getName

    override def orderManagers: Seq[OrderManager] = orderManagersFld

    override def initModel(modelProps: Map[String, String]) : ModelInitResult = {
        modelProperties = modelProps
        applyProperties(modelProps)
    }


    /**
     * @param intr - interval to generate ohlc
     * @param lengthToMaintain length of enabled histories
     * @return return sequence of TimeSeries objects
     */
    def enableOhlc(intr: Interval, lengthToMaintain: Int = -1): IndexedSeq[OhlcSeries] = {
        return (0 until orderManagers.length).map(bindComp.marketDataDistributor.activateOhlcTimeSeries(_, intr, lengthToMaintain))
    }

    protected def tickTopic(idx : Int) : SubChannel[Tick] = bindComp.marketDataDistributor.tickTopic(idx)

    /**
     * method to provide parameters to model
     * @param mprops - configuration params to model
     * returns false if parameters are invalid
     */
    protected def applyProperties(mprops: Map[String, String]) : ModelInitResult


    protected def listenInterval(interval : Interval, callback : Interval=>Unit): Unit = {
        bindComp.intervalService.addListener(interval,time=>callback(interval), true)
    }

    def ohlc(idx : Int, interval : Interval): OhlcSeries = bindComp.marketDataDistributor.getTs(idx,interval)


    override def onBacktestEnd() = {
        orderManagers.foreach(_.flattenAll())
    }

}
