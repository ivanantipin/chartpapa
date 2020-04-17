package firelib.core.backtest.tradegate

import firelib.common.Order
import firelib.core.TradeGate
import firelib.core.domain.reject

class TradeGateRiskManager(val maxMoneyTotal : Long, val delegate : TradeGate) :
    TradeGate {

    val prices = mutableMapOf<String,Double>()

    val positions = mutableMapOf<String,Int>()

    override fun sendOrder(order: Order) {
        val totalMm = positions.entries.map { it.value * prices[it.key]!! }.sum() +
                order.side.sign * order.qtyLots * order.instr.lot * prices[order.security]!!

        if(totalMm > maxMoneyTotal){
            order.reject("exceed max money threshold ${totalMm} > ${maxMoneyTotal}")
        }else{
            order.tradeSubscription.subscribe {
                val pos = positions.computeIfAbsent(order.security, { 0 })
                positions[order.security] = pos + it.side().sign * it.qty* order.instr.lot
            }
            delegate.sendOrder(order)
        }
    }

    fun updateBidAsks(ticker : String, price: Double) {
        prices[ticker] = price
    }

    override fun cancelOrder(order: Order) {
        delegate.cancelOrder(order)
    }

}