package firelib.common.tradegate

import firelib.domain.Ohlc

class OhlcBidAskUpdater(val updatee : (Double,  Double)=>Unit) extends (Ohlc=>Unit){

    override def apply(oh: Ohlc): Unit = {
        updatee(oh.C - 0.005,oh.C + 0.005)
    }
}




