package firelib.core

import firelib.common.Order
import firelib.common.tradegate.TradeGateStub

class TradeGateSwitch(val backtestGate: TradeGateStub) : TradeGate {
    var delegate: TradeGate = backtestGate

    fun setActiveReal(realGate: TradeGate) {
        delegate = realGate
    }

    override fun cancelOrder(order: Order) {
        delegate.cancelOrder(order)
    }

    override fun sendOrder(order: Order) {
        delegate.sendOrder(order)
    }

}