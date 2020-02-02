package firelib.common

import firelib.core.domain.InstrId
import firelib.core.misc.DurableChannel
import firelib.core.misc.dbl2Str
import firelib.core.misc.toStandardString
import firelib.core.domain.OrderState
import firelib.core.domain.OrderType
import firelib.core.domain.Side
import java.time.Instant

data class Order(val orderType: OrderType,
                 val price: Double,
                 val qtyLots: Int,
                 val side: Side,
                 val security: String,
                 val id: String,
                 val placementTime: Instant,
                 val instr: InstrId

) {
    val tradeSubscription: DurableChannel<Trade> = DurableChannel()
    val orderSubscription: DurableChannel<OrderState> = DurableChannel()

    init {
        require(qtyLots > 0) { "order qty <= 0!!" }
        require(price > 0 || orderType == OrderType.Market) { "price : $price <=  0!!" }
    }

    val longPrice = (price * 1000000).toLong()

    override fun toString(): String = "Order(price=${dbl2Str(price, 6)} qty=$qtyLots side=$side type=$orderType orderId=$id sec=$security time=${placementTime.toStandardString()}})"
}