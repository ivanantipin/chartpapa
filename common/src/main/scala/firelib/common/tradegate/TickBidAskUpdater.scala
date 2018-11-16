package firelib.common.tradegate

import firelib.domain.Tick

class TickBidAskUpdater(val updatee : (Double,  Double)=>Unit) extends (Tick=>Unit){

    override def apply(tt: Tick): Unit = {
        assert(!tt.bid.isNaN, "bid/ask can't be NaN")
        assert(!tt.ask.isNaN, "bid/ask can't be NaN")
        updatee(tt.bid,tt.ask)
    }
}
