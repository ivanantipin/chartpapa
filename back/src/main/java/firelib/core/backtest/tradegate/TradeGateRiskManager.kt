package firelib.core.backtest.tradegate

import firelib.common.Order
import firelib.core.TradeGate
import firelib.core.domain.*
import firelib.core.timeservice.TimeServiceManaged
import java.time.Instant
import kotlin.math.absoluteValue

class TradeGateRiskManager(val maxMoneyTotal : Long, val delegate : TradeGate, val instruments : List<String>) :
    TradeGate {

    val prices = DoubleArray(instruments.size, {0.0})

    val tickerToIndex = instruments.mapIndexed({idx, tick-> tick to idx}).toMap()

    val positions = mutableMapOf<String,Int>()

    override fun sendOrder(order: Order) {
        val existing = positions.entries.map { it.value * prices[tickerToIndex[it.key]!!] }.sum()
        val target = existing +
                order.side.sign * order.qtyLots * order.instr.lot * prices[tickerToIndex[order.security]!!]


        if(target > maxMoneyTotal && existing.absoluteValue < target.absoluteValue){
            order.reject("exceed max money threshold ${target} > ${maxMoneyTotal}")
        }else {
            order.tradeSubscription.subscribe {
                val pos = positions.computeIfAbsent(order.security, { 0 })
                positions[order.security] = pos + it.side().sign * it.qty* order.instr.lot
            }
            delegate.sendOrder(order)
        }
    }

    fun updateBidAsks(i: Int, time: Instant, price: Double) {
        prices[i] = price
    }

    override fun cancelOrder(order: Order) {
        delegate.cancelOrder(order)
    }

}

fun testOrder(sec : String) : Order{
    return Order(OrderType.Market, 1.0, 10, Side.Buy,sec,"id", Instant.now(), InstrId.dummyInstrument(sec), "NA")
}

//fixme switch to test
fun main() {
    val instruments = listOf("sec0", "sec1")
    val stub = TradeGateStub(instruments, TimeServiceManaged())
    val rm = TradeGateRiskManager(100L, stub, instruments)

    listOf(1.0, 2.0).forEachIndexed({ idx, price->
        rm.updateBidAsks(idx, Instant.now(), price)
        stub.updateBidAsks(idx, Instant.now(), price)
    })

    val order = testOrder("sec0").copy(qtyLots = 50)
    rm.sendOrder(order)

    require(order.orderSubscription.msgs.last.status == OrderStatus.Done)

    val order1 = testOrder("sec1").copy( qtyLots = 50)
    rm.sendOrder(order1)

    require(order1.orderSubscription.msgs.last.status == OrderStatus.Rejected, {"latest status ${order1.orderSubscription.msgs.last.status}"})

    listOf(10.0, 20.0).forEachIndexed({ idx, price->
        rm.updateBidAsks(idx, Instant.now(), price)
        stub.updateBidAsks(idx, Instant.now(), price)
    })

    //checking reducing risk functionality
    val order2 = testOrder("sec0").copy(qtyLots = 1, side = Side.Sell)
    rm.sendOrder(order2)

    require(order2.orderSubscription.msgs.last.status == OrderStatus.Done)

}