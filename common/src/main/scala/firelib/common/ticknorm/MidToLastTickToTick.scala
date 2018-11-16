package firelib.common.ticknorm

import firelib.domain.Tick

class MidToLastTickToTick extends (Tick=>Tick){
    override def apply(t: Tick): Tick = {
        t.last = (t.getBid + t.getAsk)/2
        return t
    }
}