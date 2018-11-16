package firelib.common.model

import java.time.Duration

import firelib.common.interval.Interval
import firelib.common.misc.PositionCloserByTimeOut
import firelib.common.timeseries.OhlcSeries

import scala.collection.immutable.IndexedSeq


trait MiscModelUtils{

    this : BasketModel =>

    val self = this

    def closePositionAfter(dur : Duration, idx : Int, checkEvery : Interval): PositionCloserByTimeOut ={
        val ret: PositionCloserByTimeOut = new PositionCloserByTimeOut(orderManagers(idx), dur)
        enableOhlc(checkEvery)(idx).onNewBar.subscribe(ret)
        ret
    }

    def enableFactor(name : String, fact : =>String): Unit = {
        for(om <- orderManagers){
            om.tradesTopic.subscribe(t=>{
                t.tradeStat.addFactor(name,fact)
            })
        }
    }

    implicit class IntervalListenSugar(interval : Interval){
        def listen(callback : Interval=>Unit): Unit = listenInterval(interval,callback)

        def getOhlc(instrumentIdx : Int): OhlcSeries = self.ohlc(instrumentIdx,interval)

        def enableOhlc(length : Int = -1) : IndexedSeq[OhlcSeries] = self.enableOhlc(interval,length)

        def enableOhlcAndListen( callback : Seq[OhlcSeries] => Unit, length : Int = -1): Unit = {
            val tss: IndexedSeq[OhlcSeries] = interval.enableOhlc(length)
            interval.listen(il=>{
                callback(tss)
            })
        }

    }

}
