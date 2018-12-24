package firelib.common.model

import firelib.common.interval.Interval
import firelib.common.misc.PositionCloserByTimeOut
import java.time.Duration


fun Model.closePositionAfter(dur: Duration, idx: Int, checkEvery: Interval): PositionCloserByTimeOut {
    val ret = PositionCloserByTimeOut(orderManagers()[idx], dur)
    //fixme enableOhlc(checkEvery)[idx].subscribe {ret}
    return ret
}

fun Model.enableFactor(name: String, fact: () -> String): Unit {
    for (om in orderManagers()) {
        om.tradesTopic().subscribe {
            it.tradeStat.addFactor(name, fact())
        }
    }
}

