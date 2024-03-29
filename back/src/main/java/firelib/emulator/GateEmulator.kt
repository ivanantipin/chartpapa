package firelib.emulator

import firelib.common.Order
import firelib.common.Trade
import firelib.core.InstrumentMapper
import firelib.core.TradeGate
import firelib.core.domain.InstrId
import firelib.core.domain.OrderState
import firelib.core.domain.OrderStatus
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GateEmulator(val executor: ExecutorService) : TradeGate, InstrumentMapper {

    val neworkExecutorService = Executors.newScheduledThreadPool(1)

    override fun sendOrder(order: Order) {
        if(Random.nextBoolean()){
            neworkExecutorService.schedule({
                executor.execute {
                    order.orderSubscription.publish(OrderState(order, OrderStatus.Accepted, Instant.now()))
                }
            }, 2, TimeUnit.SECONDS)

            neworkExecutorService.schedule({
                executor.execute {
                    order.tradeSubscription.publish(Trade(qty = order.qtyLots,
                        price = order.price, order = order,
                        dtGmt = Instant.now(),
                        priceTime = Instant.now()
                    ))

                    order.orderSubscription.publish(OrderState(order, OrderStatus.Done, Instant.now()))
                }
            }, 3, TimeUnit.SECONDS)
        }else{
            neworkExecutorService.schedule({
                executor.execute {
                    order.orderSubscription.publish(OrderState(order, OrderStatus.Rejected, Instant.now(), "Simple"))
                }
            }, 2, TimeUnit.SECONDS)
        }
    }

    override fun cancelOrder(order: Order) {

    }

    override fun invoke(p1: String): InstrId {
        return InstrId(code = p1)
    }

}