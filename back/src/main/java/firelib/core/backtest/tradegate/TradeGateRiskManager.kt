package firelib.core.backtest.tradegate

import firelib.common.Order
import firelib.core.TradeGate
import firelib.core.domain.*
import firelib.core.timeservice.TimeServiceManaged
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue

class TradeGateRiskManager(
    val maxMoneyTotal: Long,
    val delegate: TradeGate,
    val instruments: List<String>,
    val maxMoneyPerSerurity: Long
) :
    TradeGate {

    val log = LoggerFactory.getLogger(javaClass)

    val prices = DoubleArray(instruments.size, { 0.0 })

    val tickerToIndex = instruments.mapIndexed({ idx, tick -> tick to idx }).toMap()

    val positions = IntArray(instruments.size, {0})

    val pendingOrders = Array<MutableList<Order>>(instruments.size, { mutableListOf()})


    fun getPendingAmt(idx : Int) : Int{
        return pendingOrders[idx].sumBy { it.remainingQty() * it.side.sign }
    }


    override fun sendOrder(order: Order) {

        val idx = tickerToIndex[order.security]!!

        val adjust = order.side.sign * order.qtyLots * order.instr.lot * prices[idx]

        val secPos = ( positions[idx] + getPendingAmt(idx)) * prices[idx]

        val targetSec = secPos + adjust

        // fixme make exclusion functionality
        if (order.modelName != "RealDivModel" && targetSec > maxMoneyPerSerurity && secPos < targetSec) {
            order.reject("exceed max money per security ${targetSec} > ${maxMoneyPerSerurity}")
            return
        }

        val existing = positions.foldIndexed(0.0, {idx, acc, nel->
            acc + (nel + getPendingAmt(idx)) * prices[idx]
        })

        val target = existing + adjust

        if (target > maxMoneyTotal && existing.absoluteValue < target.absoluteValue) {
            order.reject("exceed max money threshold ${target} > ${maxMoneyTotal}")
        } else {
            order.tradeSubscription.subscribe {
                positions[idx] += it.side().sign * it.qty * order.instr.lot
            }
            pendingOrders[idx].add(order)

            order.orderSubscription.subscribe {
                if(it.order.remainingQty() == 0){
                    pendingOrders[idx].remove(it.order)
                }
                if(it.status.isFinal()){
                    pendingOrders[idx].remove(it.order)
                }
            }

            delegate.sendOrder(order)
        }
    }

    fun incPos(ticker : String, pos : Int){
        if(pos != 0){
            val idx = tickerToIndex[ticker]!!
            positions[idx] += pos
            println("current pos ${ticker} = ${positions[idx]}")
        }
    }

    fun updateBidAsks(i: Int, time: Instant, price: Double) {
        prices[i] = price
    }

    override fun cancelOrder(order: Order) {
        delegate.cancelOrder(order)
    }

}

fun testOrder(sec: String): Order {
    return Order(OrderType.Market, 1.0, 10, Side.Buy, sec, "id", Instant.now(), InstrId.dummyInstrument(sec), "NA")
}


//fixme switch to test
fun main() {
    testReduce()
    testTotalBreach()
    testPendingOrder()
}

fun testReduce(){
    val (stub, rm) = makeRiskManager()
    setPrices(rm, stub, listOf(1.0,2.0))
    val order = testOrder("sec0").copy(qtyLots = 50)
    rm.sendOrder(order)
    require(order.orderSubscription.msgs.last.status == OrderStatus.Done)
    setPrices(rm, stub, listOf(10.0,20.0))
    //checking reducing risk functionality
    val order2 = testOrder("sec0").copy(qtyLots = 1, side = Side.Sell)
    rm.sendOrder(order2)
    require(order2.orderSubscription.msgs.last.status == OrderStatus.Done)
}


fun testTotalBreach(){
    val (stub, rm) = makeRiskManager()
    setPrices(rm, stub, listOf(1.0,2.0))
    val order = testOrder("sec0").copy(qtyLots = 50)
    rm.sendOrder(order)
    require(order.status() == OrderStatus.Done)
    val order1 = testOrder("sec1").copy(qtyLots = 50)
    rm.sendOrder(order1)
    require(
        order1.orderSubscription.msgs.last.status == OrderStatus.Rejected,
        { "latest status ${order1.orderSubscription.msgs.last.status}" })
}

fun testPendingOrder(){
    val (stub, rm) = makeRiskManager()
    setPrices(rm, stub, listOf(1.0,2.0))
    val order = testOrder("sec0").copy(qtyLots = 50, orderType = OrderType.Limit, side = Side.Buy, price = 0.5)
    rm.sendOrder(order)
    require(order.status() == OrderStatus.Accepted)
    val order1 = testOrder("sec1").copy(qtyLots = 50)
    rm.sendOrder(order1)
    require(
        order1.orderSubscription.msgs.last.status == OrderStatus.Rejected,
        { "latest status ${order1.orderSubscription.msgs.last.status}" })
}


private fun setPrices(
    rm: TradeGateRiskManager,
    stub: TradeGateStub,
    prices : List<Double>
) {
    prices.forEachIndexed({ idx, price ->
        rm.updateBidAsks(idx, Instant.now(), price)
        stub.updateBidAsks(idx, Instant.now(), price)
    })
}

private fun makeRiskManager(): Pair<TradeGateStub, TradeGateRiskManager> {
    val instruments = listOf("sec0", "sec1")
    val stub = TradeGateStub(instruments, TimeServiceManaged())
    val rm = TradeGateRiskManager(100L, stub, instruments, 50)
    return Pair(stub, rm)
}