package firelib.common.model

import firelib.common.interval.Interval
import firelib.common.misc.PositionCloserByTimeOut
import java.time.Duration


fun BasketModel.closePositionAfter(dur: Duration, idx: Int, checkEvery: Interval): PositionCloserByTimeOut {
    val ret = PositionCloserByTimeOut(orderManagers()[idx], dur)
    enableOhlc(checkEvery)[idx].onNewBar().subscribe(ret)
    return ret
}

fun BasketModel.enableFactor(name: String, fact: () -> String): Unit {
    for (om in orderManagers()) {
        om.tradesTopic().subscribe {
            it.tradeStat.addFactor(name, fact())
        }
    }
}
