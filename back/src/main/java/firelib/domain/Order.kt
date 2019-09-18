package firelib.common

import firelib.common.misc.DurableChannel
import firelib.common.misc.dbl2Str
import firelib.common.misc.toStandardString
import firelib.domain.OrderState
import firelib.domain.OrderType
import firelib.domain.Side
import java.time.Instant

data class Order(val orderType: OrderType,
                 val price: Double,
                 val qty: Int,
                 val side: Side,
                 val security : String,
                 val id: String,
                 val placementTime: Instant){

    val tradeSubscription: DurableChannel<Trade> = DurableChannel()
    val orderSubscription: DurableChannel<OrderState> = DurableChannel()

    init {
        require(qty > 0) {"order qty <= 0!!"}
        require(price > 0 || orderType == OrderType.Market) {"price : $price <=  0!!"}
    }
    

    val longPrice = (price *1000000).toLong()

    override fun toString(): String = "Order(price=${dbl2Str(price,6)} qty=$qty side=$side type=$orderType orderId=$id sec=$security time=${placementTime.toStandardString()}})"
}

