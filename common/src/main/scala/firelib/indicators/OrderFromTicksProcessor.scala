package firelib.indicators

import java.time.Instant

import firelib.common.Side
import firelib.domain.Tick


class OrderInfo {
    var dt: Instant = Instant.EPOCH
    var side: Side = Side.None
    var qty: Int = 0
    var maxPrice: Double = Double.MinValue
    var minPrice: Double = Double.MaxValue
    var vwap: Double = Double.NaN
}

class OrderFromTicksProcessor (classifyWithBidAsk  : Boolean = false) extends (Tick=>Seq[OrderInfo]){

    var lastTick: Tick = new Tick()

    private val classifySide: Tick => Side = if (classifyWithBidAsk) classifyByBidAsk else classifyByPrev

    private var currOrderInfo : OrderInfo = null

    def apply(tick: Tick) : Seq[OrderInfo] = {
        val cSide = classifySide(tick);
        var ret : Seq[OrderInfo] = Nil
        if (tick.tickNumber == lastTick.tickNumber + 1 &&  tick.time == lastTick.time &&
          (cSide == Side.None || currOrderInfo.side == cSide)) {
            currOrderInfo.qty += tick.vol;
            currOrderInfo.vwap += tick.last * tick.vol;
            currOrderInfo.maxPrice = Math.max(currOrderInfo.maxPrice, tick.last);
            currOrderInfo.minPrice = Math.min(currOrderInfo.minPrice, tick.last);
        }
        else {
            if(currOrderInfo != null){
                currOrderInfo.vwap /= currOrderInfo.qty
                assert(currOrderInfo.vwap <= currOrderInfo.maxPrice && currOrderInfo.vwap >= currOrderInfo.minPrice)
                ret = List(currOrderInfo)
            }
            currOrderInfo = new OrderInfo
            currOrderInfo.qty = tick.vol
            currOrderInfo.maxPrice = tick.last
            currOrderInfo.minPrice = tick.last
            currOrderInfo.vwap = tick.vol * tick.last;
            currOrderInfo.dt = tick.time
            currOrderInfo.side = cSide
        }
        lastTick = tick;
        ret
    }



    private def classifyByPrev(tick: Tick): Side = {
        return if(compareDbls(tick.last, lastTick.last) > 0) Side.Buy else Side.Sell
    }

    private def classifyByBidAsk(tick: Tick): Side = {
        var c = compareDbls(tick.last, tick.bid);
        if (c == 0) {
            return Side.Sell;
        }
        c = compareDbls(tick.last, tick.ask);
        if (c == 0) {
            return Side.Buy;
        }
        return Side.None;
    }

    private def compareDbls(a: Double, b: Double): Int = {
        val ep = 0.00000001;
        var d = a - b;
        if (d > ep) {
            return 1;
        }
        if (d < -ep) {
            return -1;
        }
        return 0;
    }

}
