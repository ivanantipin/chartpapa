package firelib.common.ticknorm

import firelib.domain.Tick

class NormBidAskTickFunc extends (Tick=>Tick){
    override def apply(tt: Tick): Tick = {
        if(tt.bid.isNaN){
            tt.setBid(tt.last)
        }
        if(tt.ask.isNaN){
            tt.setAsk(tt.last)
        }
        tt
    }
}


