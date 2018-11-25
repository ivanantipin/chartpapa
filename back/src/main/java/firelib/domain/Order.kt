package firelib.common

import firelib.common.misc.DurableChannel
import firelib.common.misc.SubChannel
import firelib.common.misc.dbl2Str
import firelib.common.misc.toStandardString
import firelib.domain.OrderState
import java.time.Instant

data class Order(val orderType: OrderType,
                 val price: Double,
                 val qty: Int,
                 val side: Side,
                 val security : String,
                 val id: String,
                 val placementTime: Instant){

    val tradeSubscription: DurableChannel<Trade> = DurableChannel<Trade>()
    val orderSubscription: DurableChannel<OrderState> = DurableChannel<OrderState>()

    init {
        assert(qty > 0, {"order qty <= 0!!"})
        assert(price > 0 || orderType == OrderType.Market, {"price : $price <=  0!!"})
        assert(id != null, {"id is null!!"})
        assert(security != null, {"security is null!!"})
        assert(placementTime != null, {"placement time is null!!"})
    }
    

    val longPrice = (price *1000000).toLong()

    override fun toString(): String = "Order(price=${dbl2Str(price,6)} qty=$qty side=$side type=$orderType orderId=$id sec=$security time=${placementTime.toStandardString()}})"
}

